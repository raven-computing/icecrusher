/* 
 * Copyright (C) 2020 Raven Computing
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raven.icecrusher.application;

import java.util.List;
import java.util.Stack;

import com.raven.icecrusher.application.Controller.ArgumentBundle;
import com.raven.icecrusher.application.Layout.Transition;
import com.raven.icecrusher.base.Activity;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Base class for an application which uses stacked activities.<br>
 * This class extends <code>javafx.application.Application</code>.<br>
 * The standard life-cycle applies.
 *
 */
public abstract class StackedApplication extends Application implements ChangeListener<Number> {

    /** Bundle key which can be passed to the first activity on the stack to determine 
     *  the width of the scene inside the main stage **/
    public static final String BUNDLE_KEY_SCENE_WIDTH = "scene.width";
    /** Bundle key which can be passed to the first activity on the stack to determine 
     *  the height of the scene inside the main stage **/
    public static final String BUNDLE_KEY_SCENE_HEIGHT = "scene.height";

    private static Stage mainStage;
    private static Stack<Entry> stack;
    private static String cwd;

    /**
     * Lock for activity transitions.
     * A transition is finsished with the end of its animation
     */
    private static volatile boolean transitionInProgress;

    public StackedApplication(){
        //default no-arg constructor
    }

    @Override
    public final void start(Stage stage) throws Exception{
        mainStage = stage;
        cwd = System.getProperty("user.dir");
        stack = new Stack<>();
        onStart(stage);
        Platform.runLater(() -> setupMainStage());
        mainStage.show();
    }

    /**
     * The main entry point for the application. This method is executed
     * on the JavaFX application thread 
     * 
     * @param stage The primary stage for this application
     * @throws Exception If an irreparable error occurs
     */
    public abstract void onStart(Stage stage) throws Exception;

    @Override
    public final void stop(){
        onStop();
    }

    /**
     * Called when the application should stop. When this method gets invoked, all
     * activities on the stack have either implicitly or explicitly approved of 
     * this operation and already taken the appropriate steps according to their 
     * implementation.<br>
     * This method is executed on the JavaFX application thread 
     */
    public void onStop(){ }

    @Override
    public void changed(ObservableValue<? extends Number> observable,
            Number oldValue, Number newValue){

        final double w = mainStage.getWidth();
        final double h = mainStage.getHeight();
        stack.peek().getController().onWindowResized(w, h);
        onWindowResized(w, h);
    }

    /**
     * Called when the window of the main stage of this application gets resized in either
     * its width or height or both dimensions. Subclasses of <code>StackedApplication</code>
     * may override this method to perform an action in the case of such an event in order
     * to implement a specific behaviour of the application
     * 
     * @param width The new width of the main stages' window
     * @param height The new height of the main stages' window
     */
    public void onWindowResized(final double width, final double height){ }

    /**
     * Starts the specified activity and puts it on top of the activity-stack.<br>
     * This call will cause this activity's controller class to go through the necessary
     * life-cycle methods
     * 
     * @param activity The activity to start
     */
    public static final void startActivity(final Activity activity){
        startActivity(activity, null);
    }

    /**
     * Starts the specified activity and puts it on top of the activity-stack.<br>
     * This call will cause this activity's controller class to go through the necessary
     * life-cycle methods. The specified arguments will be passed to the target activity's
     * <code>onStart()</code> method
     * 
     * @param activity The activity to start
     * @param args The arguments to be passed over to the target activity
     */
    public static final void startActivity(final Activity activity, final ArgumentBundle args){
        final Layout layout = Layout.of(activity);
        if(stack.isEmpty()){
            startFirst(layout, args);
            return;
        }
        if(isActivityTransitionInProgress()){
            return;
        }
        setActivityTransitionInProgress(true);
        final Parent target = layout.load();
        final Controller controller = layout.getController();
        final Entry current = stack.peek();
        final StackPane rootPane = getRootPane();
        final List<Node> nodes = rootPane.getChildren();
        //make sure that any global view like snackbar or spinner loading 
        //indicator does not get covered by the added node
        final int index = (nodes.indexOf(current.getParent())+1);
        //add right above the calling activity layout, but with bounds check
        rootPane.getChildren().add(((index < nodes.size()) ? index : nodes.size()-1), target);
        final double height = mainStage.getScene().getHeight();
        final Timeline timeline = Layout.getTimelineAnimationFor(
                Transition.SLIDE_DOWN, height, target, null);
        
        timeline.setOnFinished((e) -> {
            stack.push(Entry.of(target, controller));
            rootPane.getChildren().remove(current.getParent());
            setActivityTransitionInProgress(false);
        });
        timeline.play();
        current.getController().onPause();
        controller.onStart(args);
    }

    /**
     * Finishes the activity currently on top of the activity-stack and removes it from it.<br>
     * This call will cause this activity's controller class to go through the necessary
     * life-cycle methods
     * 
     */
    public static final void finishActivity(){
        finishActivity(null);
    }

    /**
     * Finishes the activity currently on top of the activity-stack and removes it from it.<br>
     * This call will cause this activity's controller class to go through the necessary
     * life-cycle methods. The specified arguments will be passed to the target activity's
     * <code>onResume()</code> method
     * 
     * @param args The arguments to be passed over to the target activity
     */
    public static final void finishActivity(ArgumentBundle args){
        if(isActivityTransitionInProgress()){
            return;
        }
        setActivityTransitionInProgress(true);
        final Entry finishedEntry = stack.pop();
        final Parent old = finishedEntry.getParent();
        final Parent current = stack.peek().getParent();
        final Controller controller = stack.peek().getController();
        if(args == null){
            args = new ArgumentBundle();
        }
        args.setFinishedController(finishedEntry.getController().getClass().getSimpleName());

        getRootPane().getChildren().add(getRootPane().getChildren().indexOf(old), current);
        final double height = mainStage.getScene().getHeight();
        final Timeline timeline = Layout.getTimelineAnimationFor(
                Transition.SLIDE_UP, height, null, old);
        
        timeline.setOnFinished((e) -> {
            getRootPane().getChildren().remove(old);
            setActivityTransitionInProgress(false);
        });
        timeline.play();
        finishedEntry.getController().onStop();
        controller.onResume(args);
    }

    /**
     * Gets the main stage of this application
     * 
     * @return A reference to the main <code>Stage</code> of this application
     */
    public static Stage getMainStage(){
        return mainStage;
    }

    /**
     * Gets the main scene of this application
     * 
     * @return A reference to the main <code>Scene</code> of this application
     */
    public static Scene getMainScene(){
        return mainStage.getScene();
    }

    /**
     * Gets the root node of the scene graph of this application
     * 
     * @return A reference to the root <code>StackPane</code> of this application
     */
    public static StackPane getRootPane(){
        return (StackPane) mainStage.getScene().getRoot();
    }

    /**
     * Instigates the shut-down process of this application. An exit-signal is sent
     * to the activity currently on top of the stack. That activity can still abort 
     * the termination by returning the appropriate signal. If the activity approves of
     * the shut-down process, <code>Platform.exit()</code> is being called, causing
     * this applications' <code>onStop()</code> method to be called, and then perform
     * final termination
     */
    public static final void exit(){
        if(stack.peek().getController().onExitRequested()){
            Platform.exit();
        }
    }

    /**
     * Gets the working directory of this application, as defined by 
     * <code>System.getProperty("user.dir")</code> at initialization time.
     * 
     * @return The working directory of this application or null if the underlying 
     *         property was not set by the system
     */
    public static final String getApplicationDirectory(){
        return cwd;
    }

    /**
     * Returns a {@link ProxyController} for the currently active Controller, i.e. the 
     * Controller currently on top of the activity stack, which can then be used to remotely 
     * invoke methods on the active <code>Controller</code>.<br>
     * 
     * <p>The Proxy can be used to invoke a public method on the underlying Controller
     * instance as long as the activity of that Controller has not finished.<br>
     * Please note that remotely invokable methods must be exposed and marked as such by the 
     * returned Controller type, meaning that you cannot call any arbitrary method.
     * 
     * @return A <code>ProxyController</code> capable of invoking properly exposed methods of 
     *         the currently active Controller
     */
    public static final ProxyController getActiveController(){
        return new DefaultProxyController(stack.peek().getController().getClass());
    }

    /**
     * Gets the root node of the local scene graph of the activity currently on top of 
     * the activity stack
     * 
     * @return A reference to the root <code>Parent</code> of the activity in the foreground
     */
    protected static Parent getLocalRootPane(){
        return stack.peek().getParent();
    }

    /**
     * Gets the root node of the local scene graph of the activity of the provided 
     * Controller
     * 
     * @param controller The <code>Controller</code> instance of the activity in question
     * @return A reference to the root <code>Parent</code> of the controller's activity or
     *         null if the specified Controller is not in the activity stack
     */
    protected static Parent getActivityRootPane(final Controller controller){
        for(final Entry e : stack){
            if(e.getController() == controller){
                return e.getParent();
            }
        }
        return null;
    }

    protected static ProxyController getProxyController(final Class<? extends Controller> type) 
            throws ControllerNotFoundException{

        final String className = type.getSimpleName();
        for(final Entry e : stack){
            if(e.getController().getClass().getSimpleName().equals(className)){
                return new DefaultProxyController(type);
            }
        }
        throw new ControllerNotFoundException("No controller class with "
                + "the specified name is in the stack: " + className);

    }

    protected static Controller getControllerByClass(final Class<? extends Controller> type) 
            throws ControllerNotFoundException{

        final String className = type.getSimpleName();
        for(final Entry e : stack){
            if(e.getController().getClass().getSimpleName().equals(className)){
                return e.getController();
            }
        }
        throw new ControllerNotFoundException("No controller class with "
                + "the specified name is in the stack: " + className);
    }

    private void setupMainStage(){
        mainStage.widthProperty().addListener(this);
        mainStage.heightProperty().addListener(this);
        mainStage.setOnCloseRequest((e) -> {
            e.consume();
            exit();
        });
    }

    private static boolean isActivityTransitionInProgress(){
        return transitionInProgress;
    }

    private static void setActivityTransitionInProgress(final boolean inProgress){
        transitionInProgress = inProgress;
    }

    private static void startFirst(final Layout activity, final ArgumentBundle args){
        final Parent root = activity.load();
        final Controller controller = activity.getController();
        Double width = null;
        Double height = null;
        if(args != null){
            width = (Double) args.getArgument(BUNDLE_KEY_SCENE_WIDTH);
            height = (Double) args.getArgument(BUNDLE_KEY_SCENE_HEIGHT);
        }
        if((width != null) && (height != null)){
            mainStage.setScene(new Scene(root, width, height));
        }else{
            mainStage.setScene(new Scene(root));
        }
        stack.push(Entry.of((Parent)(root.getChildrenUnmodifiable().get(0)), controller));
        controller.onStart(args);
    }

    /**
     * Entry to be placed on the activity-stack.
     *
     */
    private static class Entry extends Pair<Parent, Controller> {

        private static final long serialVersionUID = 1L;

        private Entry(Parent key, Controller value) {
            super(key, value);
        }

        private Parent getParent(){
            return getKey();
        }

        private Controller getController(){
            return getValue();
        }

        private static Entry of(Parent layout, Controller controller){
            return new Entry(layout, controller);
        }
    }

}

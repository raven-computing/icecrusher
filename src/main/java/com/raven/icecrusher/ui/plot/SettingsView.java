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

package com.raven.icecrusher.ui.plot;

import java.util.Set;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXRippler;
import com.raven.icecrusher.application.Cache;
import com.raven.icecrusher.ui.SceneShowingProperty;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A view for displaying information about a chart item and to let the user
 * change specific settings of that item.
 *
 */
public class SettingsView extends HBox {

    /**
     * Listener interface for actions related to user interactions with settings
     * elements of a <code>SettingsView</code> implementation.
     *
     */
    public interface ViewListener {

        /**
         * Called when the user types text in the text field related to an item label
         * 
         * @param view The <code>SettingsView</code> of the action
         * @param newLabel The new label of the settings item
         */
        void onRelabel(SettingsView view, String newLabel);

        /**
         * Called when the user changes the color of a settings item
         * 
         * @param view The <code>SettingsView</code> of the action
         * @param newColor The new color of the settings item
         */
        void onColorChanged(SettingsView view, String newColor);

        /**
         * Called when the user removes a settings item by having interacted with the 
         * corresponding control view
         * 
         * @param view The <code>SettingsView</code> of the action
         */
        void onRemove(SettingsView view);
    }

    protected static final long DEFAULT_ANIM_X_OFFSET = 330l;
    protected static final double DEFAULT_ANIM_DURATION_IN = 400;
    protected static final double DEFAULT_ANIM_DURATION_OUT = 400;
    protected static final double DEFAULT_ANIM_DURATION_RM_BTN = 500;
    protected static final double DEFAULT_ANIM_DURATION_UP = 400;

    private static final Tooltip TIP_REMOVE_BUTTON = new Tooltip("Remove");
    private static final Tooltip TIP_COLORPICKER= new Tooltip("Change color");
    
    private static final String[] colors = new String[]{
            "#1668ff","#f3622d","#57b757","#fba71b",
            "#41a9c9","#4258c9","#9a42c8","#c84164"};

    private JFXButton btnRemove;
    private TextField txt;
    private String label;
    private JFXColorPicker cp;
    private ViewListener delegate;

    private int index;

    /**
     * Constructs a new <code>SettingsView</code> with the specified 
     * index. No label will be shown for this settings view
     * 
     * @param index The index of the settings item
     */
    public SettingsView(final int index){
        super(10);//spacing of 10
        this.index = index;
        getStyleClass().add("settingsview");
        setAlignment(Pos.TOP_RIGHT);
    }

    /**
     * Constructs a new <code>SettingsView</code> with the specified 
     * label and index
     * 
     * @param index The index of the settings item
     * @param itemLabel The label of the settings item to construct
     */
    public SettingsView(final int index, final String itemLabel){
        this(index);
        this.label = itemLabel;
        final Label label = new Label(itemLabel);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMaxWidth(120.0);
        getChildren().add(label);
    }

    /**
     * Adds an editable text field to this settings view
     */
    public void addTextFieldToSettings(){
        addTextFieldToSettings(null);
    }
    
    /**
     * Adds an editable text field with the specified text to this settings view
     * 
     * @param label The text of the text field to be added
     */
    public void addTextFieldToSettings(final String label){
        if(label != null){
            this.label = label;
            this.txt = new TextField(label);
        }else{
            this.txt = new TextField(this.label);
        }
        this.txt.setPromptText("Label");
        this.txt.textProperty().addListener((observable, oldValue, newValue) -> {
            if(label != null){
                Cache.session().set(editTextCacheKey(label), newValue);
            }
            if(delegate != null){
                delegate.onRelabel(this, newValue);
            }
        });
        getChildren().add(txt);
    }
    
    /**
     * Adds a color picker to this settings view
     */
    public void addColorPickerToSettings(){
        addColorPickerToSettings(null);
    }
    
    /**
     * Adds a color picker to this settings view with its color value set
     * to the specified cache key
     * 
     * @param cacheKey The cache key of the color to restore. May be null
     */
    public void addColorPickerToSettings(final String cacheKey){
        final Color color = colorOf((cacheKey != null)
                ? Cache.session().get(colorPickerCacheKey(cacheKey),
                        colors[index%colors.length])
                : colors[index%colors.length]) ;
        
        this.cp = new JFXColorPicker(color);
        this.cp.setTooltip(TIP_COLORPICKER);
        HBox.setMargin(cp, new Insets(3.0, 0.0, 0.0, 0.0));
        this.cp.setOnAction((e) -> {
            final String newColor = getColor();
            if(cacheKey != null){
                Cache.session().set(colorPickerCacheKey(cacheKey), newColor);
            }
            cp.lookupAll(".color-box").stream().forEach((node) -> {
                node.setStyle("-fx-background-color: " + newColor + ";");
            });
            if(delegate != null){
                delegate.onColorChanged(this, newColor);
            }
        });
        
        ensureInitialColorIsSet();
        getChildren().add(cp);
    }

    /**
     * Adds a button, which is used to remove the item, to this settings view
     */
    public void addRemoveButtonToSettings(){
        this.btnRemove = new JFXButton("X");
        this.btnRemove.setTooltip(TIP_REMOVE_BUTTON);
        this.btnRemove.setOnAction((e) -> {
            if(delegate != null){
                delegate.onRemove(this);
            }
        });
        getChildren().add(btnRemove);
    }

    public void animateIn(){
        animateIn(DEFAULT_ANIM_X_OFFSET, DEFAULT_ANIM_DURATION_IN);
    }

    public void animateIn(final long offsetX, final double duration){
        final Timeline t1 = new Timeline(
                new KeyFrame(Duration.ZERO, 
                        new KeyValue(translateXProperty(), offsetX, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(duration), 
                        new KeyValue(translateXProperty(), 0, Interpolator.EASE_BOTH)));

        if(btnRemove != null){
            this.btnRemove.setOpacity(0.0);
            final Timeline t2 = new Timeline(
                    new KeyFrame(Duration.ZERO, 
                            new KeyValue(btnRemove.opacityProperty(), 0.0)),
                    new KeyFrame(Duration.millis(DEFAULT_ANIM_DURATION_RM_BTN), 
                            new KeyValue(btnRemove.opacityProperty(), 1.0)));

            new SequentialTransition(t1, t2).play();
        }else{
            t1.play();
        }
    }

    public void animateOut(final EventHandler<ActionEvent> eventHandler){
        animateOut(DEFAULT_ANIM_X_OFFSET, DEFAULT_ANIM_DURATION_OUT, eventHandler);
    }

    public void animateOut(final long offsetX, final double duration, 
            final EventHandler<ActionEvent> eventHandler){

        final Timeline t1 = new Timeline(
                new KeyFrame(Duration.ZERO, 
                        new KeyValue(translateXProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(duration), 
                        new KeyValue(translateXProperty(), offsetX, Interpolator.EASE_BOTH)));

        if(eventHandler != null){
            t1.setOnFinished(eventHandler);
        }
        t1.play();
    }

    public void animateUp(final long offsetY, final EventHandler<ActionEvent> eventHandler){
        final Timeline t1 = new Timeline(
                new KeyFrame(Duration.ZERO, 
                        new KeyValue(translateYProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(DEFAULT_ANIM_DURATION_UP), 
                        new KeyValue(translateYProperty(), -offsetY, Interpolator.EASE_BOTH)));

        t1.setOnFinished((e) -> {
            //reset Y translation
            setTranslateY(0.0);
            if(eventHandler != null){
                eventHandler.handle(e);
            }
        });
        t1.play();
    }
    
    public String editTextCacheKey(final String text){
        return "SettingsView.textField.text." + text;
    }
    
    public String colorPickerCacheKey(final String text){
        return "SettingsView.colorPicker.color." + text;
    }

    public void setViewListener(final ViewListener delegate){
        this.delegate = delegate;
    }

    public String getLabel(){
        return this.label;
    }

    public String getColor(){
        return "#" + cp.getValue().toString().substring(2, 8);
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(final int index){
        this.index = index;
    }

    public String getEditText(){
        return this.txt.getText();
    }
    
    public void setEditText(final String text){
        this.txt.setText(Cache.session().get(editTextCacheKey(text), text));
    }
    
    /**
     * Ensures that the initial color of the ColorPicker color-box pane
     * is set to the correct value
     */
    private void ensureInitialColorIsSet(){
        //take action as soon as the color picker
        //becomes attached to the scene
        SceneShowingProperty.of(cp).whenShown(
                (e) -> Platform.runLater(() -> ensureInitialColorIsSet0()));
        
    }
    
    private void ensureInitialColorIsSet0(){
        //As we are using the JFX version, the background pane to adjust might
        //not be attached when this method is called. We work around this issue
        //by detecting when new nodes are added as children to the color picker.
        //we then manually loop through the list to find the right node and set
        //its background color via styling
        final Set<Node> set = cp.lookupAll(".color-box");
        if(!set.isEmpty()){
            set.stream().forEach((node) -> {
                final Pane pane = (Pane)node;
                pane.setStyle("-fx-background-color: " + getColor() + ";");
            });
        }else{//fallback
            this.cp.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>(){
                @Override
                public void onChanged(Change<? extends Node> c){
                    boolean found = false;
                    for(final Node n1 : c.getList()){
                        if(n1 instanceof JFXRippler){
                            for(final Node n2 : ((JFXRippler)n1).getChildrenUnmodifiable()){
                                if(n2 instanceof Pane){
                                    final Pane pane = (Pane)n2;
                                    pane.setStyle("-fx-background-color: " + getColor() + ";");
                                    cp.getChildrenUnmodifiable().removeListener(this);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if(found){
                            break;
                        }
                    }
                }
            }); 
        }
    }
    
    private Color colorOf(final String colorCode){
        try{
            return Color.valueOf(colorCode);
        }catch(IllegalArgumentException ex){
            return Color.valueOf(colors[index%colors.length]);
        }
    }
}

/**
 * Adapter class for a <code>SettingsView.ViewListener</code>.
 *
 */
class ViewListenerAdapter implements SettingsView.ViewListener {

    @Override
    public void onRelabel(SettingsView view, String newLabel){ }

    @Override
    public void onColorChanged(SettingsView view, String newColor){ }

    @Override
    public void onRemove(SettingsView view){ }

}

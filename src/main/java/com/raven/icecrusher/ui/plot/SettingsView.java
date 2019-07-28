/* 
 * Copyright (C) 2019 Raven Computing
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

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
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
            "#f3622d","#fba71b","#57b757","#41a9c9",
            "#4258c9","#9a42c8","#c84164","#888888"};

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
    public SettingsView( final int index, final String itemLabel){
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
        this.txt = new TextField(this.label);
        this.txt.setPromptText("Label");
        this.txt.textProperty().addListener((observable, oldValue, newValue) -> {
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
        this.cp = new JFXColorPicker(Color.valueOf(colors[index%colors.length]));
        this.cp.setId("color-picker-box-" + String.valueOf((index)%colors.length));
        this.cp.setTooltip(TIP_COLORPICKER);
        HBox.setMargin(cp, new Insets(3.0, 0.0, 0.0, 0.0));
        this.cp.setOnAction((e) -> {
            final String newColor = getColor();
            cp.lookupAll(".color-box").stream().forEach((node) -> {
                node.setStyle("-fx-background-color: " + newColor + ";");
            });
            if(delegate != null){
                delegate.onColorChanged(this, newColor);
            }
        });
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
        this.txt.setText(text);
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

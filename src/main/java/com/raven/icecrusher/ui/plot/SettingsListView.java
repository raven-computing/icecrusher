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

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A view for displaying a vertical list of <code>SettingsView</code> controls.<br>
 * This view takes care of controlling the animations of SettingsViews when adding or 
 * removing them from the list.<br>
 * When removing a SettingsView, subsequent calls to <code>getChildren()</code> will
 * return the list of child nodes without the node that was removed even when the 
 * removal animation has not yet finished. 
 *
 */
public class SettingsListView extends VBox {

    private ObservableList<Node> proxies;
    private boolean animateResetAction;
    private boolean isRemoving;
    private boolean isAdding;

    public SettingsListView(){ }

    /**
     * Adds the specified SettingsView to this list
     * 
     * @param view The <code>SettingsView</code> to add
     */
    public void addSettingsView(final SettingsView view){
        getChildren().add(view);
        view.animateIn();
    }

    /**
     * Adds all specified SettingsViews to this list
     * 
     * @param views The List of <code>SettingsViews</code> to add
     */
    public void addAllSettingsViews(final List<SettingsView> views){
        //trigger animations when layoutChildren() gets called
        this.isAdding = true;
        getChildren().addAll(views);
        for(final SettingsView view : views){
            //makes sure all individual views are out of the scene
            //before the animation starts
            view.setTranslateX(SettingsView.DEFAULT_ANIM_X_OFFSET);
        }
    }

    /**
     * Sets this SettingsListView to the specified SettingsViews without animations
     * 
     * @param views The List of <code>SettingsViews</code> to set
     */
    public void setAllSettingsViews(final List<SettingsView> views){
        //prevent animations in layoutChildren() method
        this.isAdding = false;
        getChildren().setAll(views);
    }

    /**
     * Removes the specified SettingsView from this list
     * 
     * @param view The <code>SettingsView</code> to remove
     */
    public void removeSettingsView(final SettingsView view){
        final ObservableList<Node> nodes = getChildren();
        this.proxies = FXCollections.observableList(new ArrayList<Node>(nodes));
        final int index = view.getIndex();
        this.proxies.remove(index);
        this.isRemoving = true;
        view.animateOut((e) -> {
            //the height of each SettingsView plus the VBox spacing
            final long offsetY = (long)(view.getHeight() + getSpacing());
            if((index+1)>=nodes.size()){
                nodes.remove(index);
                isRemoving = false;
                proxies = null;
                reindexSettingsList();
            }else{
                //move all SettingsViews below the given up (if any)
                //the lowest one has to complete the removal 
                for(int i=index+1; i<nodes.size(); ++i){		
                    if(i<nodes.size()-1){
                        ((SettingsView)nodes.get(i)).animateUp(offsetY, null);
                    }else{//the last view
                        ((SettingsView)nodes.get(i)).animateUp(offsetY, (event) -> {
                            nodes.remove(index);
                            isRemoving = false;
                            proxies = null;
                            reindexSettingsList();
                        });
                    }
                }
            }
        });
    }

    /**
     * Resets and clears this list of all <code>SettingsView</code> controls
     */
    public void resetSettingsList(){
        if(animateResetAction){
            final long offsetX = SettingsView.DEFAULT_ANIM_X_OFFSET;
            final double duration = SettingsView.DEFAULT_ANIM_DURATION_OUT;
            final double speed = (offsetX/duration);
            //animate all SettingsViews
            final ObservableList<Node> nodes = getChildren();
            for(int i=0; i<nodes.size(); ++i){
                final SettingsView view = (SettingsView) nodes.get(i);
                if(i<nodes.size()-1){
                    view.animateOut((offsetX+(i*400)), (duration+((i*400)/speed)), null);
                }else{//the last view has to clear the list
                    view.animateOut((offsetX+(i*400)), (duration+((i*400)/speed)), (e) -> {
                        getChildren().clear();
                    });
                }
            }
        }else{//no animation required
            getChildren().clear();
        }
    }

    /**
     * Indicates whether a removal animation is still in process
     * 
     * @return True if an animation is still in process, false otherwise
     */
    public boolean isRemoving(){
        return this.isRemoving;
    }

    /**
     * Indicates whether this SettingsListView has any SettingsViews set
     * 
     * @return True if this <code>SettingsListView</code> has no <code>SettingsView</code>
     *         elements in its list, false otherwise
     */
    public boolean isEmpty(){
        return this.getChildren().isEmpty();
    }

    /**
     * Sets whether to animate all SettingsViews when <code>resetSettingsList()</code>
     * is being called.<br> Default is false
     * 
     * @param animateResetAction Whether to animate all list items
     */
    public void setAnimateResetAction(final boolean animateResetAction){
        this.animateResetAction = animateResetAction;
    }

    /**
     * Gets the <code>SettingsView</code> at the specified index
     * 
     * @param index The index of the SettingsView to get
     * @return The <code>SettingsView</code> at the specified index
     */
    public SettingsView getSettingsViewAt(final int index){
        return (SettingsView) getChildren().get(index);
    }

    @Override
    public ObservableList<Node> getChildren(){
        return (isRemoving ? proxies : super.getChildren());
    }

    @Override
    protected void layoutChildren(){
        super.layoutChildren();
        //triggers all SettingsViews to enter the scene successively
        if(isAdding){
            final long offsetX = SettingsView.DEFAULT_ANIM_X_OFFSET;
            final double duration = SettingsView.DEFAULT_ANIM_DURATION_IN;
            final double speed = (offsetX/duration);
            int i = 0;
            for(final Node node : getChildren()){
                final SettingsView view = (SettingsView) node;
                view.animateIn((offsetX+(i*100)), (duration+((i*100)/speed)));
                ++i;
            }
            this.isAdding = false;
        }
    }

    private void reindexSettingsList(){
        final ObservableList<Node> views = getChildren();
        for(int i=0; i<views.size(); ++i){
            ((SettingsView)views.get(i)).setIndex(i);
        }
    }
}

/* 
 * Copyright (C) 2021 Raven Computing
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

package com.raven.icecrusher.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * An <code>ObservableBooleanValue</code> indicating whether a scene is
 * attached and showing. Instances of this class wrap a scene property of
 * a component. Users can then add listeners that notify about scene
 * visibility changes. Method calls may be directly delegated to the
 * wrapped ReadOnlyObjectProperty object.
 *
 */
public class SceneShowingProperty implements ObservableBooleanValue {
    
    private ReadOnlyObjectProperty<Scene> prop;
    private ChangeListener<? super Boolean> listener;
    private ChangeListener<Scene> change;
    
    private SceneShowingProperty(final ReadOnlyObjectProperty<Scene> prop){
        this.prop = prop;
    }

    @Override
    public void addListener(InvalidationListener listener){
        prop.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener){
        prop.removeListener(listener);
    }

    @Override
    public void addListener(ChangeListener<? super Boolean> listener){
        this.listener = listener;
        this.change = new ChangeListener<Scene>(){
            @Override
            public void changed(ObservableValue<? extends Scene> observable,
                    Scene oldValue, Scene newValue){
                
                if(newValue != null){
                    final Window window = newValue.getWindow();
                    if(window != null){
                        final boolean value = window.isShowing();
                        listener.changed(SceneShowingProperty.this, !value, value);
                    }
                }
            }
        };
        this.prop.addListener(this.change);
    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> listener){
        if(this.listener != null){
            this.prop.removeListener(this.change);
            this.listener = null;
            this.change = null;
        }
    }
    
    @Override
    public Boolean getValue(){
        return get();
    }

    @Override
    public boolean get(){
        final Scene scene = prop.get();
        if(scene != null){
            final Window window = scene.getWindow();
            if((window != null) && window.isShowing()){
                return true;
            }
        }
        return false;
    }

    public void whenShown(final EventHandler<ActionEvent> handler){
        this.prop.addListener(new ChangeListener<Scene>(){
            @Override
            public void changed(ObservableValue<? extends Scene> observable,
                    Scene oldValue, Scene newValue){
                
                if(newValue != null){
                    final Window window = newValue.getWindow();
                    if((window != null) && window.isShowing()){
                        observable.removeListener(this);
                        handler.handle(new ActionEvent(SceneShowingProperty.this, null));
                    }
                }
            }
        });
    }
    
    public static SceneShowingProperty of(final Node node){
        if(node == null){
            throw new IllegalArgumentException("Node must not be null");
        }
        return SceneShowingProperty.of(node.sceneProperty());
    }

    public static SceneShowingProperty of(final ReadOnlyObjectProperty<Scene> prop){
        if(prop == null){
            throw new IllegalArgumentException("ObjectProperty must not be null");
        }
        return new SceneShowingProperty(prop);
    }
}

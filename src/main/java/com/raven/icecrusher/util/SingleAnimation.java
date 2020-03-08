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

package com.raven.icecrusher.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Simple utility class for creating node animations based on the 
 * <i>-text-color-anim</i> CSS style. This is currently only used for starting
 * color change animations on very specific nodes.
 * 
 */
public class SingleAnimation {

    private Timeline timeline;

    public SingleAnimation(final Node node, final String startColor, 
            final String endColor, final Duration duration){

        final ObjectProperty<Color> animColor = new SimpleObjectProperty<>();
        final KeyFrame kf1 = new KeyFrame(Duration.ZERO, 
                new KeyValue(animColor, Color.valueOf(startColor), Interpolator.EASE_BOTH));

        final KeyFrame kf2 = new KeyFrame(duration, 
                new KeyValue(animColor, Color.valueOf(endColor), Interpolator.EASE_BOTH));
        this.timeline = new Timeline(kf1, kf2);

        animColor.addListener((ov, oldColor, newColor) -> {
            node.setStyle(String.format("-text-color-anim: #%02x%02x%02x; ",
                    (int)(newColor.getRed()*255),
                    (int)(newColor.getGreen()*255),
                    (int)(newColor.getBlue()*255)));
        });
        this.timeline.setAutoReverse(true);
        this.timeline.setCycleCount(Animation.INDEFINITE);
    }

    public void start(){
        this.timeline.play();
    }

    public void stop(){
        this.timeline.stop();
        this.timeline = null;
    }

}

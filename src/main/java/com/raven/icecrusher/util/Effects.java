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

package com.raven.icecrusher.util;

import javafx.scene.paint.Color;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;

/**
 * Utility class for creating effects. This class cannot be instantiated.
 * Use the provided static utility methods.
 * 
 */
public final class Effects {
    
    private Effects(){ }
    
    /**
     * Gets a lighting effect which can be applied to the tabs pane node
     * for showing a drag and drop mark for files
     * 
     * @param x The x coordinate for the light position
     * @param y The y coordinate for the light position
     * @return A <code>Lighting</code> effect object to be applied to a Node
     */
    public static Lighting dragAndDropLighting(final double x, final double y){
        final Lighting lighting = new Lighting();
        lighting.setLight(new Light.Spot(x, y, 500.0, 1.0,
                Color.rgb(150, 208, 255, 0.9)));
        
        lighting.setSurfaceScale(1.0);
        lighting.setDiffuseConstant(2.0);
        lighting.setSpecularConstant(1.0);
        return lighting;
    }

}

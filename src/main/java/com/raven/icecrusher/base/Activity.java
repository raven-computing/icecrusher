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

package com.raven.icecrusher.base;

/**
 * Enumerates all activities and their respective FXML Controller file.
 *
 */
public enum Activity {

    MAIN            ("Frame.fxml"),
    PREFERENCES     ("Preferences.fxml"),
    ABOUT           ("About.fxml"),
    FEEDBACK        ("Feedback.fxml"),
    PIE_CHART       ("PlotPieChart.fxml"),
    LINE_CHART      ("PlotLineChart.fxml"),
    AREA_CHART      ("PlotAreaChart.fxml"),
    BAR_CHART       ("PlotBarChart.fxml");

    public String fxml;

    Activity(final String fxml){
        this.fxml = fxml;
    }
}

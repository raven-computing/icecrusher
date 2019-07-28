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

package com.raven.icecrusher.io.update;

import com.raven.icecrusher.util.Const;

/**
 * Defines a version as it is used by an application.<br>
 * A version is comprised of the following components:<br>
 * <pre>
 * {major}.{minor}.{patch}
 * </pre>
 *
 */
public class Version implements Comparable<Version> {

    private int major;
    private int minor;
    private int patch;

    public Version(final String version){
        final String[] v = version.split("\\.", 3);
        if(v.length >= 3){
            this.major = Integer.valueOf(v[0]);
            this.minor = Integer.valueOf(v[1]);
            this.patch = Integer.valueOf(v[2].replaceAll("[^\\d.]", ""));
        }
    }

    public Version(final int major, final int minor, final int patch){
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor(){
        return this.major;
    }

    public int getMinor(){
        return this.minor;
    }

    public int getPatch(){
        return this.patch;
    }

    @Override
    public int compareTo(final Version o) {
        if(this.major != o.getMajor()){
            return (this.major - o.getMajor());
        }
        if(this.minor != o.getMinor()){
            return (this.minor - o.getMinor());
        }
        if(this.patch != o.getPatch()){
            return (this.patch - o.getPatch());
        }
        return 0;
    }

    @Override
    public String toString(){
        return String.valueOf(this.major) + "." 
                + String.valueOf(this.minor) + "."
                + String.valueOf(this.patch);
    }

    public static Version current(){
        return new Version(Const.APPLICATION_VERSION);
    }
}

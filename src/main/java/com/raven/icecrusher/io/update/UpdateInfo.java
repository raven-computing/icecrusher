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

package com.raven.icecrusher.io.update;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.raven.icecrusher.util.ExceptionHandler;

/**
 * Models a data object retrieved over the network regarding release information.
 *
 */
public class UpdateInfo {

    public enum OperatingSystem {
        LINUX,
        WINDOWS;
    }

    public enum InstructionType {
        COPY_LOCAL,
        COPY_REMOTE;
    }

    public enum PackageType {
        APP,
        FULL;
    }

    private Document document;

    private UpdateInfo(final InputSource is) 
            throws SAXException, IOException, ParserConfigurationException{

        final DocumentBuilder builder = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder();

        this.document = builder.parse(is);
    }

    /**
     * Gets the version of this UpdateInfo object
     * 
     * @return The version of this UpdateInfo object as a <code>Version</code>
     */
    public Version getVersion(){
        final String content = getMainValueByTag("version");
        if((content == null) || (content.isEmpty())){
            return null;
        }
        final String[] s = content.toString().split("\\.", 3);
        int major = 0;
        int minor = 0;
        int patch = 0;
        if(s.length >= 3){
            major = Integer.valueOf(s[0]);
            minor = Integer.valueOf(s[1]);
            patch = Integer.valueOf(s[2].replaceAll("[^\\d.]", ""));
        }
        return new Version(major, minor, patch);
    }

    /**
     * Gets the date of this UpdateInfo object
     * 
     * @return The date of this UpdateInfo object as a String
     */
    public String getDate(){
        final String date = getMainValueByTag("date");
        return ((date == null) || (date.isEmpty()) ? null : date);
    }

    /**
     * Gets the runtime ID of this UpdateInfo object
     * 
     * @return The runtime ID of this UpdateInfo object as an int
     */
    public int getRuntimeId(){
        final String id = getMainValueByTag("runtime");
        if((id != null) && (!id.isEmpty())){
            try{
                return Integer.valueOf(id);
            }catch(NumberFormatException ex){}
        }
        return -1;
    }

    /**
     * Gets the instruction object of the specified OS of this UpdateInfo object
     * 
     * @param instructionOS The operating system of the update instruction to get
     * @return The update instruction for the specified OS of this UpdateInfo
     *         object as an <code>Instruction</code> object
     */
    public Instruction getInstruction(final OperatingSystem instructionOS){
        String osAttribute = null;
        Instruction instruct = null;
        switch(instructionOS){
        case LINUX:
            osAttribute = "Linux";
            break;
        case WINDOWS:
            osAttribute = "Windows";
            break;
        }
        final NodeList nodes = this.document.getElementsByTagName("instruction");
        for(int i=0; i<nodes.getLength(); ++i){
            final Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                final Element element = (Element) node;
                final String os = element.getAttribute("os");
                if((os != null) && (os.equals(osAttribute))){
                    Node n = element.getElementsByTagName("copy").item(0);
                    InstructionType type = null;
                    if(n != null){
                        switch(n.getTextContent()){
                        case "Local":
                            type = InstructionType.COPY_LOCAL;
                            break;
                        case "Remote":
                            type = InstructionType.COPY_REMOTE;
                            break;
                        }
                    }
                    n = element.getElementsByTagName("checksum").item(0);
                    final String checksum = ((n != null) ? n.getTextContent() : null);
                    n = element.getElementsByTagName("id").item(0);
                    final String id = ((n != null) ? n.getTextContent() : null);
                    instruct = new Instruction(instructionOS, type, checksum, id);
                    break;
                }
            }
        }
        return instruct;
    }

    /**
     * Gets the downloadable package checksum of the specified OS and package type of 
     * this UpdateInfo object
     * 
     * @param os The operating system of the package checksum to get
     * @param packageType The package type of the checksum to get
     * @return The package checksum for the specified OS and package type of this 
     *         UpdateInfo object as a String
     */
    public String getPackageChecksum(OperatingSystem os, PackageType packageType){
        final NodeList nodes = this.document.getElementsByTagName("checksum");
        for(int i=0; i<nodes.getLength(); ++i){
            final Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                final Element element = (Element) node;
                final String attrOs = element.getAttribute("os");
                final String attrPack = element.getAttribute("package");
                if((attrOs.equalsIgnoreCase(os.toString())) 
                        && (attrPack.equalsIgnoreCase(packageType.toString()))){

                    return element.getTextContent();
                }
            }
        }
        return null;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        final String nl = System.lineSeparator();
        String s = this.getDate();
        if((s != null) && (!s.isEmpty())){
            sb.append("Date: ");
            sb.append(s);
            sb.append(nl);
        }
        final Version v = this.getVersion();
        if(v != null){
            sb.append("Version: ");
            sb.append(v.toString());
            sb.append(nl);
        }
        final int id = this.getRuntimeId();
        sb.append("Runtime-ID: ");
        sb.append((id >= 0) ? id : "N/A");
        sb.append(nl);
        s = this.getPackageChecksum(OperatingSystem.LINUX, PackageType.APP);
        if((s != null) && (!s.isEmpty())){
            sb.append("Checksum: ");
            sb.append(s);
            sb.append(" (Linux, app)");
            sb.append(nl);
        }
        s = this.getPackageChecksum(OperatingSystem.LINUX, PackageType.FULL);
        if((s != null) && (!s.isEmpty())){
            sb.append("Checksum: ");
            sb.append(s);
            sb.append(" (Linux, full)");
            sb.append(nl);
        }
        s = this.getPackageChecksum(OperatingSystem.WINDOWS, PackageType.APP);
        if((s != null) && (!s.isEmpty())){
            sb.append("Checksum: ");
            sb.append(s);
            sb.append(" (Windows, app)");
            sb.append(nl);
        }
        s = this.getPackageChecksum(OperatingSystem.WINDOWS, PackageType.FULL);
        if((s != null) && (!s.isEmpty())){
            sb.append("Checksum: ");
            sb.append(s);
            sb.append(" (Windows, full)");
            sb.append(nl);
        }
        Instruction instruct = this.getInstruction(OperatingSystem.LINUX);
        if(instruct != null){
            sb.append("[Instruction]");
            sb.append(nl);
            sb.append(instruct.toString());
        }
        instruct = this.getInstruction(OperatingSystem.WINDOWS);
        if(instruct != null){
            sb.append("[Instruction]");
            sb.append(nl);
            sb.append(instruct.toString());
        }
        return sb.toString();
    }

    private String getMainValueByTag(final String tag){
        final Node node = this.document.getElementsByTagName(tag).item(0);
        if(node == null){
            return null;
        }
        final String content = node.getTextContent();
        return ((content == null) || (content.isEmpty()) ? null : content);
    }

    protected static UpdateInfo fromXmlString(final String xml){
        try{
            return new UpdateInfo(new InputSource(new StringReader(xml)));
        }catch(SAXException | IOException | ParserConfigurationException ex){
            ExceptionHandler.handle(ex);
        }
        return null;
    }

    /**
     * Models a data object holding information about update instructions for a 
     * specific operating system.
     *
     */
    public static class Instruction {

        private OperatingSystem os;
        private InstructionType type;
        private String checksum;
        private int id;

        protected Instruction(final OperatingSystem os, final InstructionType type, 
                final String checksum, final String id){

            this.os = os;
            this.type = type;
            this.checksum = checksum;
            try{
                this.id = Integer.valueOf(id);
            }catch(NumberFormatException ex){ }
        }

        /**
         * Gets the OS of this Instruction object
         * 
         * @return The operating system as an <code>OperatingSystem</code> enum
         */
        public OperatingSystem getOperatingSystem(){
            return this.os;
        }

        /**
         * Gets the instruction type of this Instruction object
         * 
         * @return The type as an <code>InstructionType</code> enum
         */
        public InstructionType getType(){
            return this.type;
        }

        /**
         * Gets the downloadable update instruction file checksum
         * 
         * @return The checksum of the downloadable update instruction file
         */
        public String getFileChecksum(){
            return this.checksum;
        }

        /**
         * Gets the ID of the released update instruction file
         * 
         * @return The ID of the update instruction file
         */
        public int getID(){
            return this.id;
        }

        @Override
        public String toString(){
            final StringBuilder sb = new StringBuilder();
            final String nl = System.lineSeparator();
            sb.append("OS: ");
            sb.append(this.getOperatingSystem());
            sb.append(nl);
            sb.append("ID: ");
            sb.append(this.getID());
            sb.append(nl);
            sb.append("Copy: ");
            sb.append(this.getType());
            sb.append(nl);
            sb.append("Checksum: ");
            sb.append(this.getFileChecksum());
            sb.append(nl);
            return sb.toString();
        }
    }

}

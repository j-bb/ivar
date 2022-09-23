/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.fwk.files.persistence;

/*
.
This is produced by compiler.
 */
import ivar.fwk.BusinessObject;

public class FusionTemplate implements BusinessObject, java.io.Serializable {

////////////////////////////////////////////
/////// Compiler put section for you ///////
/////// Technical attributes section ///////
////////////////////////////////////////////
    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * Default technical primary key.
     */
    private Long id;

////////////////////////////////////////////
/////// Compiler put section for you ///////
/////// Business attributes section  ///////
////////////////////////////////////////////
    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * Spec built-in true Java built-in true Data reference false Calculated false Spec collection false Spec mandatory true
     */
    private Long template;

    private SerializableFileIndex templateFileIndex;

////////////////////////////////////////////
/////// Compiler put section for you ///////
/////// Constructor(s)       section ///////
////////////////////////////////////////////
    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * Default constructor for FusionTemplate
     */
    public FusionTemplate() {
    }

////////////////////////////////////////////
/////// Compiler put section for you ///////
/////// Business accessors section   ///////
////////////////////////////////////////////
    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * Generated from generateAccessorForSimpleAttribute
     * <p/>
     * simple setter for template
     *
     * @param value Long to set template
     */
    public void setTemplate(final Long value) {
        this.template = value;
    }

    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * simple getter for template
     *
     * @return Long
     */
    public Long getTemplate() {
        return template;
    }

    public void setTemplateFileIndex(final SerializableFileIndex fileIndex) {
        templateFileIndex = fileIndex;
    }

    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * useful inside the create method on server side..
     */
    public Long getId() {
        return id;
    }

////////////////////////////////////////////
/////// Compiler put section for you ///////
/////// toString() section           ///////
////////////////////////////////////////////
    /**
     * .
     * <p/>
     * This is produced by compiler.
     * <p/>
     * <p/>
     * toString() will handle all attributes.
     */
    public String toString() {
        final StringBuilder st = new StringBuilder();
        st.append("\nBusinessObject composer_ui_templates.FusionTemplate\n");
        st.append("     id : " + id + "\n");
        st.append("     Long TemplateFile = ");
        st.append("     " + getTemplate());
        st.append("\n");
        st.append(super.toString());
        return st.toString();
    }
}

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
package ivar.helper.oo;

import java.lang.reflect.Field;

/**
 * Work in progress. An attempt to clone an instance of Clazz. The idea is also to add a boolean deep or int deep parameter somewhere.
 *
 * Status: do not use. R&D in progress.
 *
 * @author jbb
 * @param <Clazz>
 */
public class CloneHelper<Clazz> {

    public Clazz clone(Clazz instance) {
        Class sourceClass = instance.getClass();
        Field[] sourceFields = sourceClass.getFields();
        Clazz destinationInstance = null;

//        try {
//            destinationInstance = (Clazz) sourceClass.newInstance();
//        } catch (InstantiationException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        Field[] destinationFields = destinationInstance.getClass().getFields();
//        for (int i = 0; i < destinationFields.length; i++) {
//            final Field destinationField = destinationFields[i];
//            final Field sourceField = sourceFields[i];
//
//            int sourceModifiers = sourceField.getModifiers();
//            if (!Modifier.isFinal(sourceModifiers)) {
//                Object value = null;
//                try {
//                    value = sourceField.get(instance);
//                } catch (IllegalAccessException e) {
//                    System.out.println("CloneHelper : problem reading " + sourceClass.getName() + " :: " + sourceField.getName());
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//                try {
//
//                    destinationField.set(destinationInstance, value);
//                } catch (IllegalAccessException e) {
//                    System.out.println("CloneHelper : problem seting " + destinationInstance.getClass().getName() + " :: " + destinationField.getName());
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//            } else {
//                System.out.println("CloneHelper : isFInal : "+sourceClass.getName() + " :: " + sourceField.getName());
//            }
//        }
        return destinationInstance;
    }
}

package com.alibaba.testable.agent.util;

import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.testable.agent.constant.ByteCodeConst.*;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class MethodUtil {

    /**
     * Judge whether a method is static
     * @param mn method to check
     * @return is static or not
     */
    public static boolean isStatic(MethodNode mn) {
        return (mn.access & ACC_STATIC) != 0;
    }

    /**
     * parse method desc, fetch parameter types
     * @param desc method description
     * @return list of parameter types
     */
    public static List<Byte> getParameterTypes(String desc) {
        List<Byte> parameterTypes = new ArrayList<Byte>();
        boolean travelingClass = false;
        boolean travelingArray = false;
        for (byte b : desc.getBytes()) {
            if (travelingClass) {
                if (b == CLASS_END) {
                    travelingClass = false;
                    travelingArray = false;
                }
            } else {
                if (isPrimaryType(b)) {
                    // should treat primary array as class (issue-48)
                    parameterTypes.add(travelingArray ? TYPE_CLASS : b);
                    travelingArray = false;
                } else if (b == TYPE_CLASS) {
                    travelingClass = true;
                    parameterTypes.add(b);
                } else if (b == TYPE_ARRAY) {
                    travelingArray = true;
                } else if (b == PARAM_END) {
                    break;
                }
            }
        }
        return parameterTypes;
    }

    /**
     * extract parameter part of method desc
     * @param desc method description
     * @return parameter value
     */
    public static String extractParameters(String desc) {
        int returnTypeEdge = desc.lastIndexOf(PARAM_END);
        return desc.substring(1, returnTypeEdge);
    }

    /**
     * parse method desc, fetch return value types
     * @param desc method description
     * @return types of return value
     */
    public static String getReturnType(String desc) {
        int returnTypeEdge = desc.lastIndexOf(PARAM_END);
        return desc.substring(returnTypeEdge + 1);
    }

    /**
     * parse method desc, fetch first parameter type (assume first parameter is an object type)
     * @param desc method description
     * @return types of first parameter
     */
    public static String getFirstParameter(String desc) {
        int typeEdge = desc.indexOf(CLASS_END);
        return typeEdge > 0 ? desc.substring(1, typeEdge + 1) : "";
    }

    /**
     * remove first parameter from method descriptor
     * @param desc original descriptor
     * @return descriptor without first parameter
     */
    public static String removeFirstParameter(String desc) {
        return "(" + desc.substring(desc.indexOf(";") + 1);
    }

    /**
     * add extra parameter to the beginning of method descriptor
     * @param desc original descriptor
     * @param type byte code class name
     * @return descriptor with specified parameter at begin
     */
    public static String addParameterAtBegin(String desc, String type) {
        return "(" + type + desc.substring(1);
    }

    private static boolean isPrimaryType(byte b) {
        return b == TYPE_BYTE || b == TYPE_CHAR || b == TYPE_DOUBLE || b == TYPE_FLOAT
            || b == TYPE_INT || b == TYPE_LONG || b == TYPE_SHORT || b == TYPE_BOOL;
    }

    /**
     * format to java style constructor descriptor
     * @param owner class of method belongs to
     * @param desc method constructor in bytecode format
     * @return java style constructor descriptor
     */
    public static String toJavaDesc(String owner, String desc) {
        String parameters = MethodUtil.extractParameters(desc);
        return String.format("%s(%s)", owner, parameters);
    }

    /**
     * format to java style method descriptor
     * @param owner class of method belongs to
     * @param name method name
     * @param desc method descriptor in bytecode format
     * @return java style method descriptor
     */
    public static String toJavaDesc(String owner, String name, String desc) {
        String returnType = MethodUtil.getReturnType(desc);
        String parameters = MethodUtil.extractParameters(desc);
        return String.format("%s %s::%s(%s)", returnType, owner, name, parameters);
    }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2021 Rice University, Baylor College of Medicine, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package emt.clt;

import emt.Globals;
import emt.clt.tools.*;


/**
 * Factory for command line tools to call different functions
 *
 * @author Muhammad Shamim
 * @since 1/30/2015
 */
public class CLTFactory {

    public static void generalUsage() {
        System.out.println("Hi-C EMT Version " + Globals.versionNum);
        System.out.println("Usage:");
        System.out.println("\t" + "-h, --help print help");
        System.out.println("\t" + "-v, --verbose verbose mode");
        System.out.println("\t" + "-V, --version print version");
        System.out.println("Tools: excise, stitch, info/validate, equals");
        System.out.println("Type hic_emt <tool_name> for more detailed usage instructions");
    }

    public static CLT getCLTCommand(String cmd) {

        cmd = cmd.toLowerCase();
        if (cmd.startsWith("stitch")) {
            return new Stitch();
        } else if (cmd.startsWith("excise")) {
            return new Excise();
        } else if (cmd.startsWith("info") || cmd.startsWith("validate")) {
            return new Info();
        } else if (cmd.startsWith("equal")) {
            return new Equals();
        } else if (cmd.startsWith("compare-norms")) {
            return new CompareNorms();
        }
        return null;
    }
}

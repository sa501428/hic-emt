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

import jargs.gnu.CmdLineParser;
import javastraw.reader.type.NormalizationHandler;
import javastraw.reader.type.NormalizationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command Line Parser for Mixer commands
 *
 * @author Muhammad Shamim
 */
public class CommandLineParser extends CmdLineParser {

    private final Option verboseOption = addBooleanOption('v', "verbose");
    private final Option helpOption = addBooleanOption('h', "help");
    private final Option versionOption = addBooleanOption('V', "version");
    private final Option resetOriginOption = addBooleanOption("reset-origin");
    private final Option multipleChromosomesOption = addStringOption('c', "chromosomes");
    private final Option multipleResolutionsOption = addStringOption('r', "resolution(s)");
    private final Option threadNumOption = addIntegerOption('z', "threads");
    private final Option subsampleNumOption = addIntegerOption("subsample");
    private final Option randomSeedsOption = addStringOption("random-seeds");
    private final Option normalizationTypeOption = addStringOption('k', "normalization");

    public CommandLineParser() {
    }

    public int getSubsamplingOption() {
        return optionToInt(subsampleNumOption);
    }

    public boolean getResetOrigin() {
        return optionToBoolean(resetOriginOption);
    }

    /**
     * String Set flags
     */
    public List<String> getChromosomeListOption() {
        return optionToStringList(multipleChromosomesOption);
    }

    public List<Integer> getMultipleResolutionOptions() {
        return optionToIntegerList(multipleResolutionsOption);
    }

    public Integer getResolutionOption() {
        List<Integer> resolutions = getMultipleResolutionOptions();
        if (resolutions != null && resolutions.size() > 0) {
            return resolutions.get(0);
        }
        return null;
    }

    public long[] getMultipleSeedsOption() {
        List<String> possibleSeeds = optionToStringList(randomSeedsOption);
        if (possibleSeeds != null) {
            long[] seeds = new long[possibleSeeds.size()];
            for (int i = 0; i < seeds.length; i++) {
                seeds[i] = Long.parseLong(possibleSeeds.get(i));
            }
            return seeds;
        }
        return null;
    }

    /**
     * boolean flags
     */
    private boolean optionToBoolean(Option option) {
        Object opt = getOptionValue(option);
        return opt != null && (Boolean) opt;
    }

    public boolean getHelpOption() {
        return optionToBoolean(helpOption);
    }

    public boolean getVerboseOption() {
        return optionToBoolean(verboseOption);
    }

    public boolean getVersionOption() {
        return optionToBoolean(versionOption);
    }

    /**
     * int flags
     */
    private int optionToInt(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? 0 : ((Number) opt).intValue();
    }

    /**
     * double flags
     */

    private List<String> optionToStringList(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? null : new ArrayList<>(Arrays.asList(opt.toString().split(",")));
    }

    private List<Integer> optionToIntegerList(Option option) {
        Object opt = getOptionValue(option);
        if (opt == null) return null;
        List<String> tempList = new ArrayList<>(Arrays.asList(opt.toString().split(",")));
        List<Integer> intList = new ArrayList<>();
        for (String temp : tempList) {
            intList.add(Integer.parseInt(temp));
        }
        return intList;
    }

    /**
     * String flags
     */
    public NormalizationType getNormalizationTypeOption(NormalizationHandler normalizationHandler) {
        return retrieveNormalization(optionToString(normalizationTypeOption), normalizationHandler);
    }

    public String getNormalizationStringOption() {
        return optionToString(normalizationTypeOption);
    }

    /**
     * String flags
     */
    private String optionToString(Option option) {
        Object opt = getOptionValue(option);
        return opt == null ? null : opt.toString();
    }

    private NormalizationType retrieveNormalization(String norm, NormalizationHandler normalizationHandler) {
        if (norm == null || norm.length() < 1)
            return null;

        try {
            return normalizationHandler.getNormTypeFromString(norm);
        } catch (IllegalArgumentException error) {
            System.err.println("Normalization must be one of \"NONE\", \"VC\", \"VC_SQRT\", \"KR\", or \"SCALE\".");
            System.exit(7);
        }
        return null;
    }
}
// Copyright (c) Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.pomutil.cleaner.web;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.kdgcommons.util.SimpleCLIParser.OptionDefinition;

import com.kdgregory.pomutil.cleaner.Cleaner;
import com.kdgregory.pomutil.cleaner.CommandLine;
import com.kdgregory.pomutil.cleaner.CommandLine.Options;


/**
 *  This controller is used to test cases where there's a @RequestMapping at
 *  both the class and method level.
 */
@Controller
@RequestMapping("/cleaner")
public class MainController
{
    Logger logger = Logger.getLogger(getClass());

    private static List<Options> supportedOptions = Arrays.asList(
            Options.ORGANIZE_POM,
            Options.COMMON_PROPS,
            Options.DEPENDENCY_NORMALIZE,
            Options.PLUGIN_NORMALIZE,
            Options.DEPENDENCY_SORT,
            Options.DEPENDENCY_SORT_BY_SCOPE,
            Options.VP_REPLACE_EXISTING);


    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView doGet()
    {
        logger.info("invoked via GET");
        ModelAndView mav = new ModelAndView("main");
        mav.addObject("options", new OptionTranslator().toOptionList());
        return mav;
    }


    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView doPost(
            WebRequest request,
            @RequestParam(value="file", required=true) MultipartFile file
            )
    throws Exception
    {
        OptionTranslator options = new OptionTranslator(request);
        
        logger.info("invoked via POST, options = " + options);

        InputStream in = null;
        String result = "";
        try
        {
            in = file.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new Cleaner(options.toCommandLine(), in, out).run();
            result = new String(out.toByteArray(), "UTF-8");
        }
        catch (Exception ex)
        {
            result = "unable to process POM";
        }
        finally
        {
            IOUtil.closeQuietly(in);
        }

        ModelAndView mav = new ModelAndView("main");
        mav.addObject("options", options.toOptionList());
        mav.addObject("cleanedPom", result);
        return mav;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Utility class to translate between <code>CommandLine</code> and a form
     *  that is usable by the view.
     */
    private static class OptionTranslator
    {
        ArrayList<OptionHolder> options = new ArrayList<MainController.OptionHolder>();

        /**
         *  Constructor invoked by GET requests.
         */
        public OptionTranslator()
        {
            for (Options opt : supportedOptions)
            {
                OptionDefinition def = CommandLine.getDefinition(opt);
                options.add(new OptionHolder(def, def.isEnableByDefault()));
            }
        }


        /**
         *  Constructor invoked by POST requests. Will extract option settings from
         *  request parameters.
         */
        public OptionTranslator(WebRequest request)
        {
            for (Options opt : supportedOptions)
            {
                OptionDefinition def = CommandLine.getDefinition(opt);
                boolean optValue = ! StringUtil.isBlank(request.getParameter(opt.name()));
                options.add(new OptionHolder(def, optValue));
            }
        }


        public List<OptionHolder> toOptionList()
        {
            return options;
        }


        public CommandLine toCommandLine()
        {
            String[] args = new String[options.size()];
            for (int ii = 0 ; ii < args.length ; ii++)
            {
                OptionHolder opt = options.get(ii);
                args[ii] = opt.isValue() ? opt.getDefinition().getEnableVal()
                                         : opt.getDefinition().getDisableVal();
            }
            return new CommandLine(args);
        }


        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer(256).append("[");
            for (OptionHolder opt : options)
            {
                if (opt.isValue())
                {
                    if (sb.length() > 1)
                        sb.append(",");
                    sb.append(opt.getDefinition().getKey());
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }


    /**
     *  Combines option value and definition, for use by the view.
     */
    public static class OptionHolder
    {
        private OptionDefinition definition;
        private boolean value;

        public OptionHolder(OptionDefinition definition, boolean value)
        {
            this.definition = definition;
            this.value = value;
        }

        public OptionDefinition getDefinition()
        {
            return definition;
        }

        public boolean isValue()
        {
            return value;
        }
    }
}

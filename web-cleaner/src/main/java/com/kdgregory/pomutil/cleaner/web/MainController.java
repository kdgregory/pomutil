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

import org.apache.log4j.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import net.sf.kdgcommons.io.IOUtil;

import com.kdgregory.pomutil.cleaner.Cleaner;
import com.kdgregory.pomutil.cleaner.CommandLine;


/**
 *  This controller is used to test cases where there's a @RequestMapping at
 *  both the class and method level.
 */
@Controller
@RequestMapping("/cleaner")
public class MainController
{
    Logger logger = Logger.getLogger(getClass());


    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView doGet()
    {
        logger.info("invoked via GET");
        return new ModelAndView("main");
    }


    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView doPost(
            @RequestParam(value="file", required=true) MultipartFile file
            )
    throws Exception
    {
        logger.info("invoked via POST");

        InputStream in = null;
        String result = "";
        try
        {
            in = file.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new Cleaner(new CommandLine(), in, out).run();
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
        mav.addObject("cleanedPom", result);
        return mav;
    }
}

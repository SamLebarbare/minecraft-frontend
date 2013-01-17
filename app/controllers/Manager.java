/*
 * Copyright (c) 2013, Samuel Loup. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package controllers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import play.Play;
import play.mvc.*;


import models.*;

@Check("moderator")
@With(Secure.class)
public class Manager extends Controller {

    public static void index() {
        render();
    }
    
    
    public static void carouselManager() 
    {
        render();
    }
    public static void newCarouselImage(String text,String desc,File file)
    {
        File newFile=Play.getFile("/public/images/" + file.getName());
        
        MinecraftServer mcs = MinecraftServer.find("").first();
        file.renameTo(newFile);
        file.delete();
        Image img = new Image();
        img.text = text;
        img.desc = desc;
        img.path = newFile.getName();
        img.save();
        carouselManager();
    }
    
    public static void changeConfiguration(String name,String host,Integer port,String pass)
    {
        MinecraftServer mcs = MinecraftServer.find("").first();
        mcs.name = name;
        mcs.hostIp = host;
        mcs.port = port;
        mcs.password = pass;
        mcs.save();
        configureServer();
    }
    public static void configureServer()
    {
        MinecraftServer mcs = MinecraftServer.find("").first();
        render(mcs);
    }
    
    public static void testRcon()
    {
        MinecraftServer mcs = MinecraftServer.find("").first();
        String result = mcs.testRcon();
        showRconResultTest(result);
    }
    
    
    public static void whiteList()
    {
        Whitelist wl = Whitelist.find("").first();
        MinecraftServer mcs = MinecraftServer.find("").first();
        render(wl,mcs);
    }
    
    public static void removeFromWhitelist(Long mcsId,String nickname)
    {
        Whitelist wl = Whitelist.find("").first();
        wl.remove(mcsId, nickname);
        
        Crafter toRemove = Crafter.find("nickname", nickname).first();
        toRemove.delete();
        whiteList();
    }

    public static void showRconResultTest(String result) {
        render(result);
    }

    
    

}
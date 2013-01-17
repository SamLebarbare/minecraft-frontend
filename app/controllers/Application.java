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

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import rcon.MinecraftRcon;

public class Application extends Controller {

    
    
    public static void index() 
    {
        
        
        
        List<String> onlineCrafters = new ArrayList<String>();
        List<Image> images = Image.findAll();
        
        MinecraftServer mcs = MinecraftServer.find("").first();
        onlineCrafters = mcs.getOnlineCrafters();
       
        
        render(onlineCrafters,mcs,images);
    }
    
    public static void About()
    {
        render();
    }
    public static void whispToPlayer(String nickname,String message)
    {
        MinecraftServer mcs = MinecraftServer.find("").first();
        try 
        {
            System.out.println("tell "+nickname+" "+message);
            System.out.println(MinecraftRcon.send(mcs.hostIp,mcs.port, mcs.password, "tell "+nickname+" "+message));
            
        } catch (Exception ex) 
        {
        }
        index();
    }
    
    
    public static void teleportToPlayer(String nicknameFrom,String nicknameTo)
    {
        MinecraftServer mcs = MinecraftServer.find("").first();
        try 
        {
            System.out.println("tp "+nicknameFrom+" "+nicknameTo);
            System.out.println(MinecraftRcon.send(mcs.hostIp,mcs.port, mcs.password, "tp "+nicknameFrom+" "+nicknameTo));
            
        } 
        catch (Exception ex) 
        {
        }
        index();
    }

}
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
package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import play.db.jpa.Model;
import rcon.MinecraftRcon;


@Entity
public class MinecraftServer extends Model{

    public String name;
    public String hostIp;
    public Integer port;
    public String password;
    
   

  
    
    public MinecraftServer(String name,String hostIp,String port,String pass)
    {
        this.name = name;
        this.hostIp = hostIp;
        this.port = Integer.getInteger(port);
        this.password = pass;
        
    }
    
    public String testRcon() 
    {
        try 
        {
            
            String result = MinecraftRcon.send(hostIp, port, password, "list");
            
            
            return result;
        } 
        catch (Exception e) 
        {
            return e.getMessage();
        }
    }
    
    public List<String> getOnlineCrafters()
    {
        List<String> onlinePlayers = new ArrayList<String>();
        try 
        {
            
            String result = MinecraftRcon.send(hostIp, port, password, "list");
            
            
            String[] split = result.split(":");
            
            String[] players = split[1].split(",");
            
            for(String p : players)
            {
                String pname = p.replaceAll(" ", "");
                if(!"".equals(pname))
                    onlinePlayers.add(pname);
            }
            
            return onlinePlayers;
            
        } 
        catch (Exception e) 
        {
            
        }
        
        
        return onlinePlayers;   
    }
    
}

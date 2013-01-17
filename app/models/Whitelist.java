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


import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import play.db.jpa.Model;
import rcon.MinecraftRcon;



@Entity
public class Whitelist extends Model {

    @OneToMany(mappedBy="whitelisted")
    public List<Crafter> players;

    public Whitelist() {
    }
    
    
    public void remove(Long mcsId,String nickname)
    {
        MinecraftServer mcs = MinecraftServer.findById(mcsId);
        
        try {
                System.out.println(MinecraftRcon.send(mcs.hostIp, mcs.port, mcs.password, "whitelist remove "+nickname));              
                
                
            } catch (Exception e) {
            }

        
    }
    

    
}

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

import models.Crafter;
import models.MinecraftServer;
import models.Whitelist;
import play.*;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import rcon.MinecraftRcon;


@OnApplicationStart
public class Bootstrap extends Job<Void> {

    @Override
    public void doJob() {
        // Check if the database is empty
        if (MinecraftServer.count() == 0) {

            System.out.println("Initial creation of a default server from config...");
            MinecraftServer newmcs = new MinecraftServer(Play.configuration.get("mfe.servername").toString(), Play.configuration.get("mfe.serverip").toString(), Play.configuration.get("mfe.rconport").toString(), Play.configuration.get("mfe.rconpwd").toString());
            newmcs.save();

            Whitelist newwl = new Whitelist();
            newwl.save();
            
            if(Play.configuration.get("mfe.createfromwhitelist").equals("true"))
            {
                System.out.println("Populating server whitelist manager...");
                

                MinecraftServer mcs = MinecraftServer.find("").first();
                Whitelist wl = Whitelist.find("").first();
                try {

                    String result = MinecraftRcon.send(mcs.hostIp, mcs.port, mcs.password, "whitelist list");
                    System.out.println(result);

                    String[] split = result.split(":");

                    String list = split[1].replaceAll(" ", ";");
                    System.out.println(list);
                    String[] whitelistPlayers = list.split(";");

                    for (String p : whitelistPlayers) {
                        if (p.length() > 1) {
                            System.out.println(p);
                            Crafter exist = Crafter.find("nickname", p).first();
                            if (exist == null) {
                                System.out.println("Need to create " + p);
                                Crafter c = new Crafter(p);
                                c.whitelisted = wl;
                                c.password = Play.configuration.get("mfe.defaultpwd").toString();
                                c.admin = false;
                                c.moderator = false;
                                c.save();

                            }
                        }

                    }

                } catch (Exception e) {
                }
            }
            
            
            
            //Create Front-end manager
            Crafter admin = new Crafter(Play.configuration.get("mfe.adminusr").toString());
            admin.whitelisted = newwl;
            admin.password = Play.configuration.get("mfe.adminpwd").toString();
            admin.admin = true;
            admin.moderator = true;
            admin.save();

                            
                            

        }


    }
}




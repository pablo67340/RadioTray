/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package radiotray;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Bryce
 */
public class ConfigFile {

    private final Map<String, String> stations = new HashMap<>();

    public ConfigFile() {
        stations.put("KoolFM", "http://cob-ais.leanstream.co/CKMBFM-MP3?args=web_02");
    }

    public Map<String, String> getStations() {
        return stations;
    }
}

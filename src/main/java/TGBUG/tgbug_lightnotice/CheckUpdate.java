package TGBUG.tgbug_lightnotice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CheckUpdate {
    public String getPluginVersion(String Plugin_yml_url) {
        String yamlContent = fetchContent(Plugin_yml_url);
        if (yamlContent != null) {
            return parseYAML(yamlContent).get("version");
        }
        return null;
    }

    public String checkForUpdates(String currentVersion, String username, String repo, String branch) { //username: GitHub用户名；repo：GitHub仓库名；branch：GitHub分支
        String Plugin_yml_url = "https://raw.githubusercontent.com/" + username + "/" + repo + "/" + branch + "/src/main/resources/plugin.yml";
        String latestVersion = getPluginVersion(Plugin_yml_url);
        if (latestVersion != null) {
            if (!currentVersion.equals(latestVersion)) {
                return ("有可用更新！当前版本: " + currentVersion + "，最新版本: " + latestVersion);
            } else {
                return ("您正在使用最新版本: " + currentVersion);
            }
        } else {
            return ("无法获取最新版本信息！");
        }
    }

    private String fetchContent(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 设置连接超时和读取超时
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 获取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 返回null表示获取内容失败
        }
        return response.toString();
    }


    private Map<String, String> parseYAML(String yaml) {
        Map<String, String> result = new HashMap<>();
        String[] lines = yaml.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("#")) {
                continue; // 跳过空行和注释
            }
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                result.put(key, value);
            }
        }
        return result;
    }
}



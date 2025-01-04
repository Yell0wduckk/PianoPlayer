package org.PianoPlayer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadDirUtil {
    public static List<String> findJsonFiles(String dirPath) {
        Path startingDir = Paths.get(dirPath);
        List<String> jsonFilePaths = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(startingDir)) {
            jsonFilePaths = stream
                    .filter(Files::isRegularFile) // 只选择常规文件
                    .filter(path -> path.toString().toLowerCase().endsWith(".json")) // 选择以 .json 结尾的文件
                    .map(Path::toString) // 将 Path 对象转换为字符串表示的路径
                    .collect(Collectors.toList()); // 收集到列表中
        } catch (IOException e) {
            System.err.println("An error occurred while traversing the directory: " + e.getMessage());
        }
        return jsonFilePaths;
    }


}

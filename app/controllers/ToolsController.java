package controllers;

import java.io.File;

public class ToolsController extends BaseController {

    public void download(Long id) {
        String cwd = System.getProperty("user.dir");
        File file = new File(cwd, "resource\\BackupTool-win32-ia32.zip");
        renderBinary(file);
    }
}

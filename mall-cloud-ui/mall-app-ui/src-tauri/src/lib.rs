#[cfg(desktop)]
use std::sync::Mutex;
#[cfg(desktop)]
use tauri::Manager;
#[cfg(desktop)]
use crate::core::desktop::tray::tray::create_system_tray;
use crate::commands::hello::greet;

mod commands;
mod core;
mod models;
mod plugins;
mod utils;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        // 使用设置挂钩执行与设置相关的任务
        // 在主循环之前运行，因此尚未创建任何窗口
        .setup(|app| {
            #[cfg(desktop)]
            create_system_tray(app);

            // 创建一个自动启动插件，在系统启动时自动启动您的应用程序
            // 具体使用请参考 https://v2.tauri.org.cn/plugin/autostart/
            #[cfg(desktop)]
            {
                use tauri_plugin_autostart::MacosLauncher;
                use tauri_plugin_autostart::ManagerExt;

                app.handle()
                    .plugin(tauri_plugin_autostart::init(
                        MacosLauncher::LaunchAgent,
                        Some(vec!["--flag1", "--flag2"]),
                    ))
                    .expect("TODO: panic message");

                // 获取自动启动管理器
                let autostart_manager = app.autolaunch();
                // 启用自动启动
                let _ = autostart_manager.enable();
                // 检查启用状态
                println!(
                    "registered for autostart? {}",
                    autostart_manager.is_enabled().unwrap()
                );
                // 禁用自动启动
                let _ = autostart_manager.disable();
            }

            // 添加更新插件，使用更新服务器或静态 JSON 自动更新你的 Tauri 应用程序。
            // 具体使用请参考 https://v2.tauri.org.cn/plugin/updater/
            #[cfg(desktop)]
            {
                app.handle()
                    .plugin(tauri_plugin_updater::Builder::new().build())
                    .expect("TODO: panic message");
                app.manage(plugins::desktop::updater::app_updates::PendingUpdate(
                    Mutex::new(None),
                ));
            }

            // 添加窗口状态插件，允许您保存和恢复窗口状态。保存窗口位置和大小，并在应用重新打开时恢复它们
            // 具体使用请参考 https://v2.tauri.org.cn/plugin/window-state/
            #[cfg(desktop)]
            app.handle()
                .plugin(tauri_plugin_window_state::Builder::default().build())
                .expect("TODO: panic message");

            // 添加二维码扫描插件，允许您的移动应用程序使用相机扫描二维码、EAN-13 和其他类型的条形码。
            // 具体使用请参考 https://v2.tauri.org.cn/plugin/barcode-scanner/
            #[cfg(mobile)]
            app.handle().plugin(tauri_plugin_barcode_scanner::init()).expect("TODO: panic message");

            // 添加生物识别插件，允许您的移动应用程序使用生物识别进行身份验证。例如：指纹、人脸、声纹等
            // 具体使用请参考 https://v2.tauri.org.cn/plugin/biometric/
            // #[cfg(mobile)]
            // app.handle().plugin(tauri_plugin_biometric::Builder::new().build());

            // 钩子期望一个 Ok 结果
            Ok(())
        })
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_os::init())
        .invoke_handler(tauri::generate_handler![greet])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
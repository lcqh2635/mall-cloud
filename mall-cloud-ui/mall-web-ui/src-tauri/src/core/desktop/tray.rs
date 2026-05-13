// Tauri 允许您为应用程序创建和自定义系统托盘。这可以通过提供对常用操作的快速访问来增强用户体验。
// https://v2.tauri.org.cn/learn/system-tray/

#[cfg(desktop)]
pub mod tray {
    use tauri::image::Image;
    use tauri::menu::{
        CheckMenuItem, CheckMenuItemBuilder, IconMenuItem, Menu, MenuBuilder, MenuItem,
        SubmenuBuilder,
    };
    use tauri::tray::{MouseButton, MouseButtonState, TrayIconBuilder, TrayIconEvent};
    use tauri::{App, Emitter, Manager, Wry};

    /// 创建系统托盘
    /// 想要创建一个系统托盘，请阅读 https://v2.tauri.org.cn/learn/system-tray/
    pub fn create_system_tray(app: &mut App) {
        // 创建托盘菜单，调用下面的方法
        let menu = create_tray_menu(app);

        let _tray = TrayIconBuilder::with_id("tray")
            .icon(app.default_window_icon().unwrap().clone()) // 默认的图片
            // .icon(Image::from_bytes(include_bytes!("../icons/light@2x.png")).expect("REASON")) // 自定义的图片，需要给 tauri 添加 image-png 特性
            // tooltip 为此托盘图标设置工具提示。但 linux 不支持使用此功能。
            .tooltip("Tauri App")
            .menu(&menu)
            // 监听菜单事件，每一个配置的前缀为上面的 MenuItem 中的 id
            .on_menu_event(|app, event| match event.id.as_ref() {
                "open" => {
                    // 打开事件
                    println!("open menu item was clicked");
                    // handle_open_coco(app);
                    let window = app.get_webview_window("main").unwrap();
                    let _ = window.show();
                }
                "hide" => {
                    // 隐藏事件
                    println!("hide menu item was clicked");
                    // handle_hide_coco(app);
                    let window = app.get_webview_window("main").unwrap();
                    let _ = window.hide();
                }
                "about" => {
                    // 将同步事件发出到所有 Web 视图
                    let _ = app.emit("open_settings", "about");
                }
                "settings" => {
                    // Windows 无法打开第二个窗口，问题: https://github.com/tauri-apps/tauri/issues/11144 https://github.com/tauri-apps/tauri/issues/8196
                    //#[cfg(windows)]
                    let _ = app.emit("open_settings", "");

                    // #[cfg(not(windows))]
                    // open_settings(&app);
                }
                "quit" => {
                    println!("quit menu item was clicked");
                    app.exit(0);
                }
                _ => {
                    println!("menu item {:?} not handled", event.id);
                }
            })
            // 监听托盘事件
            .on_tray_icon_event(|tray, event| match event {
                TrayIconEvent::Click {
                    button: MouseButton::Left,
                    button_state: MouseButtonState::Up,
                    ..
                } => {
                    println!("left click pressed and released");
                    // 在这个例子中，当点击托盘图标时，将展示并聚焦于主窗口
                    let app = tray.app_handle();
                    if let Some(window) = app.get_webview_window("main") {
                        let _ = window.unminimize();
                        let _ = window.show();
                        let _ = window.set_focus();
                    }
                }
                _ => {
                    println!("unhandled event {event:?}");
                }
            })
            .build(app)
            .unwrap();
    }

    /// 创建系统托盘菜单
    /// 想要定义和操作系统托盘中的菜单，请阅读 https://v2.tauri.org.cn/learn/window-menu/
    fn create_tray_menu(app: &mut App) -> Menu<Wry> {
        // 定义具体菜单项

        // 退出按钮
        let quit_i = MenuItem::with_id(app, "quit", "退出 Coco", true, None::<&str>).unwrap();
        // 设置按钮
        let settings_i = MenuItem::with_id(app, "settings", "设置", true, None::<&str>).unwrap();
        // 打开按钮
        let open_i = MenuItem::with_id(app, "open", "打开 Coco", true, None::<&str>).unwrap();
        // 关于按钮
        let about_i = MenuItem::with_id(app, "about", "关于 Coco", true, None::<&str>).unwrap();
        // 隐藏按钮
        let hide_i = MenuItem::with_id(app, "hide", "隐藏 Coco", true, None::<&str>).unwrap();
        // ......
        let dashboard_i =
            MenuItem::with_id(app, "dashboard", "仪表盘", true, None::<&str>).unwrap();

        // 从路径加载图标
        let icon_image = Image::from_bytes(include_bytes!("../../../icons/icon.png")).unwrap();

        // 创建具有子菜单的菜单项，参考 https://v2.tauri.org.cn/learn/window-menu/
        let dashboard_submenu = SubmenuBuilder::new(app, "Dashboard")
            .item(&dashboard_i)
            .item(&settings_i)
            .items(&[
                &CheckMenuItem::new(app, "CheckMenuItem 1", true, true, None::<&str>).unwrap(),
                &IconMenuItem::new(app, "IconMenuItem 2", true, Some(icon_image), None::<&str>)
                    .unwrap(),
            ])
            .build()
            .expect("TODO: panic message");

        // let icon_item = IconMenuItemBuilder::new("icon")
        //     .icon(icon_image)
        //     .build(app).unwrap();

        let lang_str = app.config().identifier.clone();
        let check_sub_item_1 = CheckMenuItemBuilder::new("English")
            .id("en")
            .checked(lang_str == "en")
            .build(app)
            .unwrap();
        let check_sub_item_2 = CheckMenuItemBuilder::new("简体中文")
            .id("en")
            .checked(lang_str == "en")
            .enabled(false)
            .build(app)
            .unwrap();
        let other_item = SubmenuBuilder::new(app, "语言切换")
            .item(&check_sub_item_1)
            .item(&check_sub_item_2)
            .build()
            .unwrap();

        // 按照一定顺序 把菜单项 MenuItem 放到菜单 Menu 里
        let menu = MenuBuilder::new(app)
            .item(&open_i)
            .separator() // 分割线
            .item(&hide_i)
            .item(&about_i)
            .item(&dashboard_submenu)
            .item(&other_item)
            .item(&settings_i)
            .separator() // 分割线
            .item(&quit_i)
            .build()
            .unwrap();

        menu
    }
}

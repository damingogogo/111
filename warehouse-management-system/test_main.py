import sys, traceback
print("=== Test Start ===")
try:
    print("Importing tkinter...")
    import tkinter as tk
    print("tkinter OK")

    print("Importing ttkbootstrap...")
    import ttkbootstrap as ttkb
    print("ttkbootstrap OK")

    print("Importing db...")
    import db
    print("db OK")

    print("Calling init_database...")
    db.init_database()
    print("init_database OK")

    print("Importing main modules...")
    import config
    from ui_login import LoginWindow
    print("All imports OK")

    print("Creating root window...")
    root = tk.Tk()
    root.withdraw()
    print("Root window created")

    print("Creating login window...")
    user_info = {"role": None}
    def on_login(user):
        user_info.update(user)
        print(f"Login callback: {user}")

    login = LoginWindow(root, on_login)
    print("LoginWindow created OK")

except Exception as e:
    print(f"ERROR: {e}")
    traceback.print_exc()
    sys.exit(1)

print("=== Test Complete ===")
input("Press Enter to exit...")

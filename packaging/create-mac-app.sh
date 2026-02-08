#!/bin/bash

# Create the app directory structure
APP_NAME="JImg2PDF"
APP_DIR="$APP_NAME.app/Contents"

mkdir -p "$APP_DIR/MacOS"
mkdir -p "$APP_DIR/Resources/Java"

# Copy the JAR file
cp ../build/libs/JImg2PDF.jar "$APP_DIR/Resources/Java/"

# Create the Info.plist file
cat > "$APP_DIR/Info.plist" << EOL
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>English</string>
    <key>CFBundleExecutable</key>
    <string>JavaAppLauncher</string>
    <key>CFBundleIconFile</key>
    <string>app.icns</string>
    <key>CFBundleIdentifier</key>
    <string>dev.xerohero.JImg2PDF</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>JImg2PDF</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>NSHumanReadableCopyright</key>
    <string>Copyright © 2024 JImg2PDF. All rights reserved.</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>NSMainNibFile</key>
    <string>MainMenu</string>
    <key>NSPrincipalClass</key>
    <string>NSApplication</string>
</dict>
</plist>
EOL

# Create the launcher script
cat > "$APP_DIR/MacOS/JavaAppLauncher" << 'EOL'
#!/bin/bash

# Get the absolute path to the app bundle
APP_PATH="$(cd "$(dirname "$0")/.." && pwd)"
JAR_PATH="$APP_PATH/Resources/Java/JImg2PDF.jar"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    osascript -e 'display dialog "Java is not installed. Please install Java to run this application." buttons {"OK"} default button 1 with icon stop'
    exit 1
fi

# Run the application
java -Xdock:name="JImg2PDF" -Xdock:icon="$APP_PATH/Resources/app.icns" -jar "$JAR_PATH" "$@"
EOL

# Make the launcher executable
chmod +x "$APP_DIR/MacOS/JavaAppLauncher"

# Create a simple icon (you can replace this with your own icon)
# This creates a basic icon - you might want to replace it with a proper icon file
if ! command -v sips &> /dev/null; then
    echo "sips command not found. Using default icon."
else
    # Create a temporary icon file
    mkdir -p temp_icon.iconset
    
    # Create icon files of various sizes
    for size in 16 32 64 128 256 512; do
        sips -z $size $size /System/Applications/Preview.app/Contents/Resources/AppIcon.icns --out temp_icon.iconset/icon_${size}x${size}.png
    done
    
    # Create the .icns file
    iconutil -c icns temp_icon.iconset -o "$APP_DIR/Resources/app.icns"
    
    # Clean up
    rm -R temp_icon.iconset
fi

echo "Created $APP_NAME.app"
echo "You can now move this to your Applications folder"

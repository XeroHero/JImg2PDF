#!/bin/bash
set -e

# Configuration
APP_NAME="JImg2PDF"
# shellcheck disable=SC2034
VERSION="1.0.0"
JAR_NAME="$APP_NAME.jar"
OUTPUT_DIR="build/macos"
APP_BUNDLE="$OUTPUT_DIR/$APP_NAME.app"
CONTENTS_DIR="$APP_BUNDLE/Contents"
MACOS_DIR="$CONTENTS_DIR/MacOS"
RESOURCES_DIR="$CONTENTS_DIR/Resources"
JAVA_DIR="$RESOURCES_DIR/Java"

# Clean and build the project
echo "Building $APP_NAME..."
./gradlew clean build

# Create app bundle structure
echo "Creating app bundle..."
rm -rf "$OUTPUT_DIR"
mkdir -p "$MACOS_DIR" "$JAVA_DIR"

# Copy the JAR file
cp "build/libs/$JAR_NAME" "$JAVA_DIR/$APP_NAME.jar"

# Create the launcher script
cat > "$MACOS_DIR/$APP_NAME" << INNER_EOF
#!/bin/bash
SCRIPT_DIR="\$(cd "\$(dirname "\$0")" && pwd)"
JAVA_APP_DIR="\$SCRIPT_DIR/../Resources/Java"
exec java -Xdock:name="JImg2PDF" -jar "\$JAVA_APP_DIR/JImg2PDF.jar" "\$@"
INNER_EOF

# Make the launcher executable
chmod +x "$MACOS_DIR/$APP_NAME"

# Create Info.plist
cat > "$CONTENTS_DIR/Info.plist" << 'PLIST_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>JImg2PDF</string>
    <key>CFBundleDisplayName</key>
    <string>JImg2PDF</string>
    <key>CFBundleIdentifier</key>
    <string>dev.xerohero.JImg2PDF</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>CFBundleExecutable</key>
    <string>JImg2PDF</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
PLIST_EOF

echo "App bundle created at: $APP_BUNDLE"
echo "To run the app: open '$APP_BUNDLE'"

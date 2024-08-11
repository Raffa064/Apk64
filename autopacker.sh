LIBS="./libs"
BIN="./bin/release/apk64"
OUTPUT="./output"
LOCKFILE="$OUTPUT/libs.lock"
OUTPUT_APK64="$OUTPUT/apk64"

# Extract libs
if [ ! -e "$LOCKFILE" ]; then
  mkdir ouput
  find "$LIBS" -regex ".*\.jar" | while read -r j; do
    unzip $j -d "$OUTPUT"
  done
  touch $LOCKFILE
fi

# Get apk64 ".class" files
rm -rf "$OUTPUT_APK64"
mkdir "$OUTPUT_APK64"
find $BIN -regex ".*\.class" | while read -r class; do
  class_name=$(basename $class)
  if [ "$class_name" != "Test.class" ]; then
    echo "$class_name"
    cp "$class" "$OUTPUT_APK64/$class_name"
  else
    echo "Skipped: $class_name"
  fi
done

# Generate jar
rm ./apk64.jar
cd $OUTPUT
zip -r ../apk64.jar ./* -x libs.lock

echo -e "\a"

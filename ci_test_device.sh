#!/system/bin/sh
MESSAGE="$1"
am start -n com.eternal.ai/.MainActivity --es TEST_MESSAGE "$MESSAGE"

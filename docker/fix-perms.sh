# update the uid
if [ -n "$opt_u" ]; then
  OLD_UID=$(getent passwd "${opt_u}" | cut -f3 -d:)
  NEW_UID=$(stat -c "%u" "$1")
  if [ "$OLD_UID" != "$NEW_UID" ]; then
    echo "Changing UID of $opt_u from $OLD_UID to $NEW_UID"
    usermod -u "$NEW_UID" -o "$opt_u"
    if [ -n "$opt_r" ]; then
      find / -xdev -user "$OLD_UID" -exec chown -h "$opt_u" {} \;
    fi
  fi
fi
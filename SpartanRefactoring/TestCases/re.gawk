#!/usr/bin/gawk -f
{
  for (i = 1; i <= NF; i++)
    p($i)
}
function p(s) {
  before = s
  gsub("^[a-zA-Z]*","git mv " before " ", s);
  print s;
}

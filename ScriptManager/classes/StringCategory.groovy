class StringCategory {
    static String lower(String s) {
        return s?.toLowerCase()
    }
    static boolean eqi(String s1, String s2) {
        return s1.equalsIgnoreCase(s2)
    }
    static String upper(String s) {
        return s?.toUpperCase()
    }
    static String cap(String string) {
        if(string == null || string.length() == 0) return string
        return string[0].toUpperCase() + string.substring(1)
    }
    static String uncap(String string) {
        if(string == null || string.length() == 0) return string
        return string[0].toLowerCase() + string.substring(1)
    }
    static File[] ls(String path) {
        return new File(path).listFiles()
    }
    static boolean insertBefore(StringBuilder str, String content, Closure cl) {
        int index = cl.doCall(str)
        if(index < 0) {
            // No match
            return false
        }
        if(index == 0) {
            str.insert(0, content)
        } else if(index > 0) {
            index = str.lastIndexOf('\n', index - 1)
            if(index >= 0) {
                str.insert(index + 1, content)
            } else {
                str.insert(0, content)
            }
        }
        return true
    }
    static boolean insertAfter(StringBuilder str, String content, Closure cl) {
        int index = cl.doCall(str)
        if(index < 0) {
            // No match
            return false
        }
        index = str.indexOf('\n', index)
        if(index < 0) {
            str.append(content)
        } else {
            str.insert(index + 1, content)
        }
        return true
    }
    
    static String block(CharSequence str, Closure cl) {
        def (begin, end) = cl(str)
        if(begin < 0 || end < 0) {
            return null
        }
        def index1 = str.lastIndexOf('\n', begin)
        def index2 = str.indexOf('\n', end)
        if(index2 < 0) {
            index2 = str.length()
        }
        str.substring(index1 + 1, index2 + 1)
    }
    
    // indexが所属した行を返却(改行含む)
    static String line(CharSequence str, int index) {
        if(index < 0) {
            return null
        }
        def begin = str.lastIndexOf('\n', index)
        def end = str.indexOf('\n', index)
        if(end < 0) {
            end = str.length()
        } else {
            end = end + 1
        }
        str.substring(begin + 1, end)
    }
    
    static void replaceAll(CharSequence str, String content, Closure cl) {
        def (start, end) = cl.doCall(str, 0)
        while(start >= 0 && end >= 0) {
            str.replace(start, end, content)
            (start, end) = cl.doCall(str, start + content.length())
        }
    }
    
    static CharSequence replaceBlock(CharSequence str, String content, Closure cl) {
        if(str instanceof StringBuilder || str instanceof StringBuffer) {
            def (start, end) = cl.doCall(str)
            if(start >= 0 && end >= 0) {
                str.replace(start, end, content)
            }
            return str
        }
        str = new StringBuilder(str)
        def (start, end) = cl.doCall(str)
        if(start >= 0 && end >= 0) {
            str.replace(start, end, content)
        }
        return str.toString()
    }
    
    static String replaceWith(CharSequence str, String regex, replacement) {
        StringBuffer buffer = new StringBuffer()
        def mch = str =~ regex
        if(replacement instanceof String) {
            while(mch.find()) {
                mch.appendReplacement(buffer, replacement)
            }
        } else if(replacement instanceof Closure){
            while(mch.find()) {
                mch.appendReplacement(buffer, replacement.doCall(mch))
            }
        } else {
            throw new Exception("第三引数不正：String Or Closureを渡してください。")
        }
        mch.appendTail(buffer)
        buffer.toString()
    }
    
    static def bound(CharSequence str, def ptn) {
        if(ptn instanceof Integer) {
            if(ptn < 0) {
                return [-1, -1]
            }
            def start = str.lastIndexOf('\n', ptn) + 1
            def end = str.indexOf('\n', ptn)
            if(end >= 0) {
                end = end + 1
            } else {
                end = str.length()
            }
            return [start, end]
        } else if(ptn instanceof CharSequence) {
            return bound(str, str.indexOf(ptn))
        } else if(ptn instanceof Closure) {
            CharSequence regex = ptn.doCall()
            return bound(str, str =~ regex)
        } else if(ptn instanceof java.util.regex.Matcher) {
            if(ptn) {
                def start = str.lastIndexOf('\n', ptn.start()) + 1
                def end = str.indexOf('\n', ptn.end())
                if(end >= 0) {
                    end = end + 1
                } else {
                    end = str.length()
                }
                return [start, end]
            } else {
                return [-1, -1]
            }
        } else {
            throw new IllegalArgumentException()
        }
    }
    
    static String col2pt(CharSequence col) {
        def tokens = col.toString().split("_")
        if(tokens.length == 1) {
            return tokens[0].toLowerCase()
        }
        
        return tokens.collect {
            it.toLowerCase().cap()
        }.join().uncap()
    }
    
    static void lines(CharSequence src, start = 0, cl) {
        int begin = start
        int end = src.indexOf('\n', begin)
        while(end >= 0) {
            if(!cl.doCall(src.substring(begin, end + 1))) {
                return
            }
            begin = end + 1
            end = src.indexOf('\n', begin)
        }
        if(begin < src.length()) {
            cl.doCall(src.substring(begin))
        }
    }
    
    static List groups(CharSequence str, int len, boolean reverse = false) {
        if(len <=0) {
            throw new IllegalArgumentException()
        }
        if(str.length() == 0) {
            return [""]
        }
        def list = []
        if(reverse) {
            for(int end = str.length(); end > 0; end = end - len) {
                list << str.substring(Math.max(end - len, 0), end)
            }
        } else {
            for(int start = 0; start < str.length(); start = start + len) {
                list << str.substring(start , Math.min(start + len, str.length()))
            }
        }
        return list
    }
    
    static String md5(CharSequence str, String encoding=System.getProperty("file.encoding")) {
        tools.md5.MD5Utils.getMD5(new ByteArrayInputStream(str.toString().getBytes(encoding)))
    }

    static int getLineNo(String text, int index) {
        int line = 1
        def array = text.toCharArray()
        for(int i=0; i<array.size() && i < index; i++) {
            if(array[i] == '\n') {
                line++
            }
        }
        return line
    }

    static String trim(String str, String chars) {
        int begin = 0
        int end = str.length()
        def list = chars.collect {it}
        while(begin < end && str[begin] in list) {
            begin++
        }
        
        while(end > begin && str[end - 1] in list) {
            end--
        }
        return str.substring(begin, end)
    }
    static def toNum(String str) {
        new BigDecimal(str)
    }

}


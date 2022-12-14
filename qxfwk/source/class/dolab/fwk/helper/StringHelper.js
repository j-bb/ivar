qx.Class.define("dolab.fwk.helper.StringHelper", {
    statics : {
        /**
         * endsWith(String str, String suffix)
         *  Test that str ends with suffix.
         */
        endsWith : function(str, suffix) {
            str = str.toLowerCase();
            suffix = suffix.toLowerCase();
            var sfxLength = suffix.length;
            var strLength = str.length;

            return (str.substr(strLength - sfxLength, sfxLength) == suffix);
        },

        /**
         * printableFileSize(size)
         * Take a size in bytes as parameter and return a human-readable string
         * composed of a size amount between 0 and 1024 (unless size is > 1024 TB)
         * and a unit suffix.
         */
        printableFileSize : function(size) {
            var prefixes = [" B", " kB", " MB", " GB", " TB"];
            var i = 0;
            for (; i < prefixes.length; i++) {
                if (size < 1024) {
                    break;
                } else {
                    size = size / 1024;
                }
            }
            size = size.toString();
            var dot = size.indexOf(".");
            if (dot >= 0) {
                size = size.substr(0, dot) + size.substr(dot, 3);
            }
            return size + prefixes[i];
        },

        /**
         * formatDate(date)
         * Take a date object as parameter and return a formatted string
         * using the standard format e.g. "12/10/12 12:32"
         */
        formatDate : function(date) {
            var formatter = new qx.util.format.DateFormat("dd/MM/yy HH:mm");
            return formatter.format(date);
        },


        //***********************
        //*********************** BEGIN SHA1 SECTION
        //***********************

        //
        // http://pajhome.org.uk/crypt/md5/sha1.html
        // distributed under BSD
        // This one was not used but look good too : http://www.movable-type.co.uk/scripts/sha1.html (LGPL)

        SHA1 : function(string) {
            return  dolab.fwk.helper.StringHelper.hex_sha1(string);
        },

        /*
         * A JavaScript implementation of the Secure Hash Algorithm, SHA-1, as defined
         * in FIPS 180-1
         * Version 2.2 Copyright Paul Johnston 2000 - 2009.
         * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
         * Distributed under the BSD License
         * See http://pajhome.org.uk/crypt/md5 for details.
         */

        /*
         * Configurable variables. You may need to tweak these to be compatible with
         * the server-side, but the defaults work in most cases.
         */
        hexcase : 0,  /* hex output format. 0 - lowercase; 1 - uppercase        */
        b64pad  : "", /* base-64 pad character. "=" for strict RFC compliance   */

        /*
         * These are the functions you'll usually want to call
         * They take string arguments and return either hex or base-64 encoded strings
         */
        hex_sha1 : function (s) {
            return dolab.fwk.helper.StringHelper.rstr2hex(dolab.fwk.helper.StringHelper.rstr_sha1(dolab.fwk.helper.StringHelper.str2rstr_utf8(s)));
        },
        b64_sha1 : function (s) {
            return dolab.fwk.helper.StringHelper.rstr2b64(dolab.fwk.helper.StringHelper.rstr_sha1(dolab.fwk.helper.StringHelper.str2rstr_utf8(s)));
        },
        any_sha1 : function (s, e) {
            return dolab.fwk.helper.StringHelper.rstr2any(dolab.fwk.helper.StringHelper.rstr_sha1(dolab.fwk.helper.StringHelper.str2rstr_utf8(s)), e);
        },
        hex_hmac_sha1 : function (k, d) {
            return dolab.fwk.helper.StringHelper.rstr2hex(dolab.fwk.helper.StringHelper.rstr_hmac_sha1(dolab.fwk.helper.StringHelper.str2rstr_utf8(k), dolab.fwk.helper.StringHelper.str2rstr_utf8(d)));
        }      ,
        b64_hmac_sha1 : function (k, d) {
            return dolab.fwk.helper.StringHelper.rstr2b64(dolab.fwk.helper.StringHelper.rstr_hmac_sha1(dolab.fwk.helper.StringHelper.str2rstr_utf8(k), dolab.fwk.helper.StringHelper.str2rstr_utf8(d)));
        }       ,
        any_hmac_sha1 : function (k, d, e) {
            return dolab.fwk.helper.StringHelper.rstr2any(dolab.fwk.helper.StringHelper.rstr_hmac_sha1(dolab.fwk.helper.StringHelper.str2rstr_utf8(k), dolab.fwk.helper.StringHelper.str2rstr_utf8(d)), e);
        }     ,

        /*
         * Perform a simple self-test to see if the VM is working
         */
        sha1_vm_test : function () {
            return dolab.fwk.helper.StringHelper.hex_sha1("abc").toLowerCase() == "a9993e364706816aba3e25717850c26c9cd0d89d";
        },

        /*
         * Calculate the SHA1 of a raw string
         */
        rstr_sha1 : function (s) {
            return dolab.fwk.helper.StringHelper.binb2rstr(dolab.fwk.helper.StringHelper.binb_sha1(dolab.fwk.helper.StringHelper.rstr2binb(s), s.length * 8));
        },

        /*
         * Calculate the HMAC-SHA1 of a key and some data (raw strings)
         */
        rstr_hmac_sha1 : function (key, data) {
            var bkey = dolab.fwk.helper.StringHelper.rstr2binb(key);
            if (bkey.length > 16) bkey = dolab.fwk.helper.StringHelper.binb_sha1(bkey, key.length * 8);

            var ipad = Array(16), opad = Array(16);
            for (var i = 0; i < 16; i++) {
                ipad[i] = bkey[i] ^ 0x36363636;
                opad[i] = bkey[i] ^ 0x5C5C5C5C;
            }

            var hash = dolab.fwk.helper.StringHelper.binb_sha1(ipad.concat(dolab.fwk.helper.StringHelper.rstr2binb(data)), 512 + data.length * 8);
            return dolab.fwk.helper.StringHelper.binb2rstr(dolab.fwk.helper.StringHelper.binb_sha1(opad.concat(hash), 512 + 160));
        },

        /*
         * Convert a raw string to a hex string
         */
        rstr2hex : function (input) {
            try {
                dolab.fwk.helper.StringHelper.hexcase
            } catch(e) {
                dolab.fwk.helper.StringHelper.hexcase = 0;
            }
            var hex_tab = dolab.fwk.helper.StringHelper.hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
            var output = "";
            var x;
            for (var i = 0; i < input.length; i++) {
                x = input.charCodeAt(i);
                output += hex_tab.charAt((x >>> 4) & 0x0F)
                        + hex_tab.charAt(x & 0x0F);
            }
            return output;
        },

        /*
         * Convert a raw string to a base-64 string
         */
        rstr2b64 : function (input) {
            try {
                dolab.fwk.helper.StringHelper.b64pad
            } catch(e) {
                dolab.fwk.helper.StringHelper.b64pad = '';
            }
            var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
            var output = "";
            var len = input.length;
            for (var i = 0; i < len; i += 3) {
                var triplet = (input.charCodeAt(i) << 16)
                        | (i + 1 < len ? input.charCodeAt(i + 1) << 8 : 0)
                        | (i + 2 < len ? input.charCodeAt(i + 2) : 0);
                for (var j = 0; j < 4; j++) {
                    if (i * 8 + j * 6 > input.length * 8) output += dolab.fwk.helper.StringHelper.b64pad;
                    else output += tab.charAt((triplet >>> 6 * (3 - j)) & 0x3F);
                }
            }
            return output;
        },

        /*
         * Convert a raw string to an arbitrary string encoding
         */
        rstr2any : function (input, encoding) {
            var divisor = encoding.length;
            var remainders = Array();
            var i, q, x, quotient;

            /* Convert to an array of 16-bit big-endian values, forming the dividend */
            var dividend = Array(Math.ceil(input.length / 2));
            for (i = 0; i < dividend.length; i++) {
                dividend[i] = (input.charCodeAt(i * 2) << 8) | input.charCodeAt(i * 2 + 1);
            }

            /*
             * Repeatedly perform a long division. The binary array forms the dividend,
             * the length of the encoding is the divisor. Once computed, the quotient
             * forms the dividend for the next step. We stop when the dividend is zero.
             * All remainders are stored for later use.
             */
            while (dividend.length > 0) {
                quotient = Array();
                x = 0;
                for (i = 0; i < dividend.length; i++) {
                    x = (x << 16) + dividend[i];
                    q = Math.floor(x / divisor);
                    x -= q * divisor;
                    if (quotient.length > 0 || q > 0)
                        quotient[quotient.length] = q;
                }
                remainders[remainders.length] = x;
                dividend = quotient;
            }

            /* Convert the remainders to the output string */
            var output = "";
            for (i = remainders.length - 1; i >= 0; i--)
                output += encoding.charAt(remainders[i]);

            /* Append leading zero equivalents */
            var full_length = Math.ceil(input.length * 8 /
                    (Math.log(encoding.length) / Math.log(2)))
            for (i = output.length; i < full_length; i++)
                output = encoding[0] + output;

            return output;
        },

        /*
         * Encode a string as utf-8.
         * For efficiency, this assumes the input is valid utf-16.
         */
        str2rstr_utf8 : function (input) {
            var output = "";
            var i = -1;
            var x, y;

            while (++i < input.length) {
                /* Decode utf-16 surrogate pairs */
                x = input.charCodeAt(i);
                y = i + 1 < input.length ? input.charCodeAt(i + 1) : 0;
                if (0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF) {
                    x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
                    i++;
                }

                /* Encode output as utf-8 */
                if (x <= 0x7F)
                    output += String.fromCharCode(x);
                else if (x <= 0x7FF)
                    output += String.fromCharCode(0xC0 | ((x >>> 6 ) & 0x1F),
                            0x80 | ( x & 0x3F));
                else if (x <= 0xFFFF)
                    output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F),
                            0x80 | ((x >>> 6 ) & 0x3F),
                            0x80 | ( x & 0x3F));
                else if (x <= 0x1FFFFF)
                    output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07),
                            0x80 | ((x >>> 12) & 0x3F),
                            0x80 | ((x >>> 6 ) & 0x3F),
                            0x80 | ( x & 0x3F));
            }
            return output;
        },

        /*
         * Encode a string as utf-16
         */
        str2rstr_utf16le : function (input) {
            var output = "";
            for (var i = 0; i < input.length; i++)
                output += String.fromCharCode(input.charCodeAt(i) & 0xFF,
                        (input.charCodeAt(i) >>> 8) & 0xFF);
            return output;
        },

        str2rstr_utf16be : function (input) {
            var output = "";
            for (var i = 0; i < input.length; i++)
                output += String.fromCharCode((input.charCodeAt(i) >>> 8) & 0xFF,
                        input.charCodeAt(i) & 0xFF);
            return output;
        },

        /*
         * Convert a raw string to an array of big-endian words
         * Characters >255 have their high-byte silently ignored.
         */
        rstr2binb : function (input) {
            var output = Array(input.length >> 2);
            for (var i = 0; i < output.length; i++)
                output[i] = 0;
            for (var i = 0; i < input.length * 8; i += 8)
                output[i >> 5] |= (input.charCodeAt(i / 8) & 0xFF) << (24 - i % 32);
            return output;
        },

        /*
         * Convert an array of big-endian words to a string
         */
        binb2rstr : function (input) {
            var output = "";
            for (var i = 0; i < input.length * 32; i += 8)
                output += String.fromCharCode((input[i >> 5] >>> (24 - i % 32)) & 0xFF);
            return output;
        },

        /*
         * Calculate the SHA-1 of an array of big-endian words, and a bit length
         */
        binb_sha1 : function (x, len) {
            /* append padding */
            x[len >> 5] |= 0x80 << (24 - len % 32);
            x[((len + 64 >> 9) << 4) + 15] = len;

            var w = Array(80);
            var a = 1732584193;
            var b = -271733879;
            var c = -1732584194;
            var d = 271733878;
            var e = -1009589776;

            for (var i = 0; i < x.length; i += 16) {
                var olda = a;
                var oldb = b;
                var oldc = c;
                var oldd = d;
                var olde = e;

                for (var j = 0; j < 80; j++) {
                    if (j < 16) w[j] = x[i + j];
                    else w[j] = dolab.fwk.helper.StringHelper.bit_rol(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1);
                    var t = dolab.fwk.helper.StringHelper.safe_add(dolab.fwk.helper.StringHelper.safe_add(dolab.fwk.helper.StringHelper.bit_rol(a, 5), dolab.fwk.helper.StringHelper.sha1_ft(j, b, c, d)),
                            dolab.fwk.helper.StringHelper.safe_add(dolab.fwk.helper.StringHelper.safe_add(e, w[j]), dolab.fwk.helper.StringHelper.sha1_kt(j)));
                    e = d;
                    d = c;
                    c = dolab.fwk.helper.StringHelper.bit_rol(b, 30);
                    b = a;
                    a = t;
                }

                a = dolab.fwk.helper.StringHelper.safe_add(a, olda);
                b = dolab.fwk.helper.StringHelper.safe_add(b, oldb);
                c = dolab.fwk.helper.StringHelper.safe_add(c, oldc);
                d = dolab.fwk.helper.StringHelper.safe_add(d, oldd);
                e = dolab.fwk.helper.StringHelper.safe_add(e, olde);
            }
            return Array(a, b, c, d, e);

        },

        /*
         * Perform the appropriate triplet combination function for the current
         * iteration
         */
        sha1_ft : function (t, b, c, d) {
            if (t < 20) return (b & c) | ((~b) & d);
            if (t < 40) return b ^ c ^ d;
            if (t < 60) return (b & c) | (b & d) | (c & d);
            return b ^ c ^ d;
        },

        /*
         * Determine the appropriate additive constant for the current iteration
         */
        sha1_kt : function (t) {
            return (t < 20) ? 1518500249 : (t < 40) ? 1859775393 :
                    (t < 60) ? -1894007588 : -899497514;
        },

        /*
         * Add integers, wrapping at 2^32. This uses 16-bit operations internally
         * to work around bugs in some JS interpreters.
         */
        safe_add : function (x, y) {
            var lsw = (x & 0xFFFF) + (y & 0xFFFF);
            var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
            return (msw << 16) | (lsw & 0xFFFF);
        },

        /*
         * Bitwise rotate a 32-bit number to the left.
         */
        bit_rol : function (num, cnt) {
            return (num << cnt) | (num >>> (32 - cnt));
        }

        //***********************
        //*********************** END SHA1 SECTION
        //***********************
    }
});
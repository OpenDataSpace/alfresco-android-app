/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)MimeTypeFile.java	1.9 07/05/14
 */

package com.sun.activation.registries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;

@SuppressWarnings("WeakerAccess")
public class MimeTypeFile {
    private final Hashtable<String, MimeTypeEntry> type_hash = new Hashtable<>();

    /**
     * The construtor that takes a filename as an argument.
     *
     * @param new_fname The file name of the mime types file.
     */
    public MimeTypeFile(String new_fname) throws IOException {
        File mime_file;
        FileReader fr;

        mime_file = new File(new_fname); // get a file object

        fr = new FileReader(mime_file);

        try {
            parse(new BufferedReader(fr));
        } finally {
            try {
                fr.close(); // close it
            } catch (IOException e) {
                // ignore it
            }
        }
    }

    public MimeTypeFile(InputStream is) throws IOException {
        parse(new BufferedReader(new InputStreamReader(is, "iso-8859-1")));
    }

    /**
     * get the MimeTypeEntry based on the file extension
     */
    @SuppressWarnings("WeakerAccess")
    MimeTypeEntry getMimeTypeEntry(String file_ext) {
        return type_hash.get(file_ext);
    }

    /**
     * Get the MIME type string corresponding to the file extension.
     */
    public String getMIMETypeString(String file_ext) {
        MimeTypeEntry entry = this.getMimeTypeEntry(file_ext);

        if (entry != null) {
            return entry.getMIMEType();
        } else {
            return null;
        }
    }

    /**
     * Parse a stream of mime.types entries.
     */
    private void parse(BufferedReader buf_reader) throws IOException {
        String line, prev = null;

        while ((line = buf_reader.readLine()) != null) {
            if (prev == null) {
                prev = line;
            } else {
                prev += line;
            }
            int end = prev.length();
            if (prev.length() > 0 && prev.charAt(end - 1) == '\\') {
                prev = prev.substring(0, end - 1);
                continue;
            }
            this.parseEntry(prev);
            prev = null;
        }
        if (prev != null) {
            this.parseEntry(prev);
        }
    }

    /**
     * Parse single mime.types entry.
     */
    private void parseEntry(String line) {
        String mime_type = null;
        String file_ext;
        line = line.trim();

        if (line.length() == 0) // empty line...
        {
            return; // BAIL!
        }

        // check to see if this is a comment line?
        if (line.charAt(0) == '#') {
            return; // then we are done!
        }

        // is it a new format line or old format?
        if (line.indexOf('=') > 0) {
            // new format
            LineTokenizer lt = new LineTokenizer(line);
            while (lt.hasMoreTokens()) {
                String name = lt.nextToken();
                String value = null;
                if (lt.hasMoreTokens() && lt.nextToken().equals("=") &&
                        lt.hasMoreTokens()) {
                    value = lt.nextToken();
                }
                if (value == null) {
                    if (LogSupport.isLoggable()) {
                        LogSupport.log("Bad .mime.types entry: " + line);
                    }
                    return;
                }
                if (name.equals("type")) {
                    mime_type = value;
                } else if (name.equals("exts")) {
                    StringTokenizer st = new StringTokenizer(value, ",");
                    while (st.hasMoreTokens()) {
                        file_ext = st.nextToken();
                        MimeTypeEntry entry = new MimeTypeEntry(mime_type, file_ext);
                        type_hash.put(file_ext, entry);
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("Added: " + entry.toString());
                        }
                    }
                }
            }
        } else {
            // old format
            // count the tokens
            StringTokenizer strtok = new StringTokenizer(line);
            int num_tok = strtok.countTokens();

            if (num_tok == 0) // empty line
            {
                return;
            }

            mime_type = strtok.nextToken(); // get the MIME type

            while (strtok.hasMoreTokens()) {
                MimeTypeEntry entry;

                file_ext = strtok.nextToken();
                entry = new MimeTypeEntry(mime_type, file_ext);
                type_hash.put(file_ext, entry);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("Added: " + entry.toString());
                }
            }
        }
    }

    // for debugging
    /*
    public static void main(String[] argv) throws Exception {
	MimeTypeFile mf = new MimeTypeFile(argv[0]);
	System.out.println("ext " + argv[1] + " type " +
						mf.getMIMETypeString(argv[1]));
	System.exit(0);
    }
    */
}


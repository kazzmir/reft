/*
 * OOWeb
 *    
 * Copyright(c)2005, OOWeb developers (see the accompanying "AUTHORS" file)
 *
 * This software is licensed under the 
 * GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1
 *    
 * For more information on distributing and using this program, please
 * see the accompanying "COPYING" file.
 */
package net.sf.ooweb.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



/**
 * MimeType utility class - return MIME type based on file extensions.
 * <p>
 * The class maintains an internal list of common MIME types based on
 * well know file extensions.  The table of internal mappings is below.
 * <p>
 * To add your own, call the static method 
 * {@link #addMimeType(String, String)}
 * 
 * <pre>
 * application/andrew-inset        ez
 * application/excel               xls
 * application/octet-stream        bin
 * application/oda                 oda
 * application/pdf                 pdf
 * application/pgp                 pgp
 * application/postscript          ps PS eps
 * application/rtf                 rtf
 * application/x-arj-compressed    arj
 * application/x-bcpio             bcpio
 * application/x-chess-pgn         pgn
 * application/x-cpio              cpio
 * application/x-csh               csh
 * application/x-debian-package    deb
 * application/x-msdos-program     com exe bat
 * application/x-dvi               dvi
 * application/x-gtar              gtar
 * application/x-gunzip            gz
 * application/x-hdf               hdf
 * application/x-latex             latex
 * application/x-mif               mif
 * application/x-netcdf            cdf nc
 * application/x-perl              pl pm
 * application/x-rar-compressed    rar
 * application/x-sh                sh
 * application/x-shar              shar
 * application/x-sv4cpio           sv4cpio
 * application/x-sv4crc            sv4crc
 * application/x-tar               tar
 * application/x-tar-gz            tgz tar.gz
 * application/x-tcl               tcl
 * application/x-tex               tex
 * application/x-texinfo           texi texinfo
 * application/x-troff             t tr roff
 * application/x-troff-man         man
 * application/x-troff-me          me
 * application/x-troff-ms          ms
 * application/x-ustar             ustar
 * application/x-wais-source       src
 * application/x-zip-compressed    zip
 * 
 * audio/basic                     snd
 * audio/midi                      mid midi
 * audio/ulaw                      au
 * audio/x-aiff                    aif aifc aiff
 * audio/x-wav                     wav
 * 
 * image/gif                       gif
 * image/ief                       ief
 * image/jpeg                      jpe jpeg jpg
 * image/png                       png
 * image/tiff                      tif tiff
 * image/x-cmu-raster              ras
 * image/x-portable-anymap         pnm
 * image/x-portable-bitmap         pbm
 * image/x-portable-graymap        pgm
 * image/x-portable-pixmap         ppm
 * image/x-rgb                     rgb
 * image/x-xbitmap                 xbm
 * image/x-xpixmap                 xpm
 * image/x-xwindowdump             xwd
 * 
 * text/html                       html htm
 * text/plain                      asc txt
 * text/css			   css
 * text/richtext                   rtx
 * text/tab-separated-values       tsv
 * text/x-setext                   etx
 * 
 * video/dl                        dl
 * video/fli                       fli
 * video/gl                        gl
 * video/mpeg                      mp2 mpe mpeg mpg
 * video/quicktime                 mov qt
 * video/x-msvideo                 avi
 * video/x-sgi-movie               movie
 * 
 * x-world/x-vrml                  vrm vrml wrl
 * </pre>
 * 
 * @author Darren Davison
 * @since 0.4
 */
public final class MimeType {

    public static final String UNKNOWN_MIME_TYPE = "unknown/unknown";
    
    private static final Map types;
    
    /* 
     * from /etc/mutt/mime.types 
     */
    static {
        // $2
        types = new HashMap();
        types.put("ez", "application/andrew-inset");
        types.put("xls", "application/excel");
        types.put("bin", "application/octet-stream");
        types.put("oda", "application/oda");
        types.put("pdf", "application/pdf");
        types.put("pgp", "application/pgp");
        types.put("ps", "application/postscript");
        types.put("rtf", "application/rtf");
        types.put("arj", "application/x-arj-compressed");
        types.put("bcpio", "application/x-bcpio");
        types.put("pgn", "application/x-chess-pgn");
        types.put("cpio", "application/x-cpio");
        types.put("csh", "application/x-csh");
        types.put("deb", "application/x-debian-package");
        types.put("com", "application/x-msdos-program");
        types.put("dvi", "application/x-dvi");
        types.put("gtar", "application/x-gtar");
        types.put("gz", "application/x-gunzip");
        types.put("hdf", "application/x-hdf");
        types.put("latex", "application/x-latex");
        types.put("mif", "application/x-mif");
        types.put("cdf", "application/x-netcdf");
        types.put("pl", "application/x-perl");
        types.put("rar", "application/x-rar-compressed");
        types.put("sh", "application/x-sh");
        types.put("shar", "application/x-shar");
        types.put("sv4cpio", "application/x-sv4cpio");
        types.put("sv4crc", "application/x-sv4crc");
        types.put("tar", "application/x-tar");
        types.put("tgz", "application/x-tar-gz");
        types.put("tcl", "application/x-tcl");
        types.put("tex", "application/x-tex");
        types.put("texi", "application/x-texinfo");
        types.put("t", "application/x-troff");
        types.put("man", "application/x-troff-man");
        types.put("me", "application/x-troff-me");
        types.put("ms", "application/x-troff-ms");
        types.put("ustar", "application/x-ustar");
        types.put("src", "application/x-wais-source");
        types.put("zip", "application/x-zip-compressed");
        types.put("snd", "audio/basic");
        types.put("mid", "audio/midi");
        types.put("au", "audio/ulaw");
        types.put("aif", "audio/x-aiff");
        types.put("wav", "audio/x-wav");
        types.put("gif", "image/gif");
        types.put("ief", "image/ief");
        types.put("jpe", "image/jpeg");
        types.put("png", "image/png");
        types.put("tif", "image/tiff");
        types.put("ras", "image/x-cmu-raster");
        types.put("pnm", "image/x-portable-anymap");
        types.put("pbm", "image/x-portable-bitmap");
        types.put("pgm", "image/x-portable-graymap");
        types.put("ppm", "image/x-portable-pixmap");
        types.put("rgb", "image/x-rgb");
        types.put("xbm", "image/x-xbitmap");
        types.put("xpm", "image/x-xpixmap");
        types.put("xwd", "image/x-xwindowdump");
        types.put("html", "text/html");
        types.put("asc", "text/plain");
        types.put("rtx", "text/richtext");
        types.put("tsv", "text/tab-separated-values");
        types.put("etx", "text/x-setext");
        types.put("dl", "video/dl");
        types.put("fli", "video/fli");
        types.put("gl", "video/gl");
        types.put("mp2", "video/mpeg");
        types.put("mov", "video/quicktime");
        types.put("avi", "video/x-msvideo");
        types.put("movie", "video/x-sgi-movie");
        types.put("vrm", "x-world/x-vrml");
        
        // $3
        types.put("eps", "application/postscript");
        types.put("PS", "application/postscript");
        types.put("bat", "application/x-msdos-program");
        types.put("exe", "application/x-msdos-program");
        types.put("nc", "application/x-netcdf");
        types.put("pm", "application/x-perl");
        types.put("texinfo", "application/x-texinfo");
        types.put("tr", "application/x-troff");
        types.put("midi", "audio/midi");
        types.put("aifc", "audio/x-aiff");
        types.put("jpeg", "image/jpeg");
        types.put("tiff", "image/tiff");
        types.put("htm", "text/html");
	types.put("css", "text/css");
        types.put("txt", "text/plain");
        types.put("m2e", "video/mpeg");
        types.put("mpe", "video/mpeg");
        types.put("mpeg", "video/mpeg");
        types.put("qt", "video/quicktime");
        types.put("vrml", "x-world/x-vrml");
        types.put("wrl", "x-world/x-vrml");
    }

    private MimeType() {
        // no instances needed
    }
    
    /**
     * Return a valid Mime type based on a file extension.
     * 
     * @param extension the file extension to query for type
     * @return the mime type for the key, or "unknown/unknown" 
     * if we don't know it
     */
    public static String getMIMEType(String extension) {
        String type = (String) types.get(extension);
        return (type == null ? UNKNOWN_MIME_TYPE : type);
    }
    
    /**
     * Returns the MIME type for a given file.
     * 
     * @param f The file
     * @return The MIME type or unknown/unknown if
     *         none could be found.
     */
    public static String getMIMEType(File f) {
        String p = f.getAbsolutePath();
        int lx = p.lastIndexOf(".");
        if (lx == -1) return UNKNOWN_MIME_TYPE;
        
        String ext = p.substring(lx + 1);
        return MimeType.getMIMEType(ext);
    }
    
    /**
     * Add or override any of the default mime types by calling this
     * method.
     * 
     * @param extension the file extension you want to associate with the
     * mime type
     * @param mimeType the mime type itself in "abc/def" format.
     */
    public static void addMimeType(String extension, String mimeType) {
        types.put(extension, mimeType);
    }

}

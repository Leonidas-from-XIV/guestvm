/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara, California 95054, U.S.A. All rights
 * reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems, licensed from the University of California. UNIX is a
 * registered trademark in the U.S. and in other countries, exclusively licensed through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered trademarks of Sun Microsystems, Inc. in the
 * U.S. and other countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and may be subject to the export or import laws in
 * other countries. Nuclear, missile, chemical biological weapons or nuclear maritime end uses or end users, whether
 * direct or indirect, are strictly prohibited. Export or reexport to countries subject to U.S. embargo or to entities
 * identified on U.S. export exclusion lists, including, but not limited to, the denied persons and specially designated
 * nationals lists is strictly prohibited.
 */
package com.sun.max.elf.xen;

import com.sun.max.elf.xen.NotesSection.DescriptorType;

/**
 * The Xen Version descriptor from the notes section of the ELF dump
 *
 * @author Puneeet Lakhina
 *
 */
public class XenVersionDescriptor extends NotesSectionDescriptor {

    public XenVersionDescriptor() {
        super(DescriptorType.XEN_VERSION);
    }

    public static final int EXTRA_VERSION_LENGTH = 16;
    public static final int CAPABILITIES_LENGTH = 1024;
    public static final int CHANGESET_LENGTH = 64;
    private long _majorVersion;
    private long _minorVersion;
    private String _extraVersion;
    private CompileInfo _compileInfo;
    private String _capabilities;
    private String _changeSet;
    private long _platformParamters;
    private long _pageSize;

    static class CompileInfo {
        public static final int COMPILE_INFO_COMPILER_LENGTH = 64;
        public static final int COMPILE_INFO_COMPILE_BY_LENGTH = 16;
        public static final int COMPILE_INFO_COMPILER_DOMAIN_LENGTH = 32;
        public static final int COMPILE_INFO_COMPILE_DATE_LENGTH = 32;
        private String _compiler;
        private String compiledBy;
        private String _compileDomain;
        private String _compileDate;

        /**
         * @return the _compiler
         */
        public String get_compiler() {
            return _compiler;
        }

        /**
         * @param compiler the _compiler to set
         */
        public void set_compiler(String compiler) {
            _compiler = compiler;
        }

        /**
         * @return the compiledBy
         */
        public String getCompiledBy() {
            return compiledBy;
        }

        /**
         * @param compiledBy the compiledBy to set
         */
        public void setCompiledBy(String compiledBy) {
            this.compiledBy = compiledBy;
        }

        /**
         * @return the _compileDomain
         */
        public String get_compileDomain() {
            return _compileDomain;
        }

        /**
         * @param compileDomain the _compileDomain to set
         */
        public void set_compileDomain(String compileDomain) {
            _compileDomain = compileDomain;
        }

        /**
         * @return the _compileDate
         */
        public String get_compileDate() {
            return _compileDate;
        }

        /**
         * @param compileDate the _compileDate to set
         */
        public void set_compileDate(String compileDate) {
            _compileDate = compileDate;
        }

    }

    /**
     * @return the _majorVersion
     */
    public long get_majorVersion() {
        return _majorVersion;
    }

    /**
     * @param majorVersion
     *            the _majorVersion to set
     */
    public void set_majorVersion(long majorVersion) {
        _majorVersion = majorVersion;
    }

    /**
     * @return the _minorVersion
     */
    public long get_minorVersion() {
        return _minorVersion;
    }

    /**
     * @param minorVersion
     *            the _minorVersion to set
     */
    public void set_minorVersion(long minorVersion) {
        _minorVersion = minorVersion;
    }

    /**
     * @return the _extraVersion
     */
    public String get_extraVersion() {
        return _extraVersion;
    }

    /**
     * @param extraVersion
     *            the _extraVersion to set
     */
    public void set_extraVersion(String extraVersion) {
        _extraVersion = extraVersion;
    }

    /**
     * @return the _compileInfo
     */
    public CompileInfo get_compileInfo() {
        return _compileInfo;
    }

    /**
     * @param compileInfo
     *            the _compileInfo to set
     */
    public void set_compileInfo(CompileInfo compileInfo) {
        _compileInfo = compileInfo;
    }

    public void set_compileInfo(String compiler,String compiledby,String compiledomain,String compileDate) {
        _compileInfo = new CompileInfo();
        _compileInfo._compileDate = compileDate;
        _compileInfo._compiler=compiler;
        _compileInfo._compileDomain=compiledomain;
        _compileInfo.compiledBy=compiledby;
    }


    /**
     * @return the _capabilities
     */
    public String get_capabilities() {
        return _capabilities;
    }


    /**
     * @param capabilities the _capabilities to set
     */
    public void set_capabilities(String capabilities) {
        _capabilities = capabilities;
    }


    /**
     * @return the _changeSet
     */
    public String get_changeSet() {
        return _changeSet;
    }


    /**
     * @param changeSet the _changeSet to set
     */
    public void set_changeSet(String changeSet) {
        _changeSet = changeSet;
    }


    /**
     * @return the _platformParamters
     */
    public long get_platformParamters() {
        return _platformParamters;
    }


    /**
     * @param platformParamters the _platformParamters to set
     */
    public void set_platformParamters(long platformParamters) {
        _platformParamters = platformParamters;
    }

    /**
     * @return the _pageSize
     */
    public long get_pageSize() {
        return _pageSize;
    }

    /**
     * @param pageSize
     *            the _pageSize to set
     */
    public void set_pageSize(long pageSize) {
        _pageSize = pageSize;
    }

}

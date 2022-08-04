/*
 *  Copyright (C) 2020-2022 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero;


/** Utility methods to check if required libraries/classes are available */
public class LibraryChecker {

    /**
     * Checks if the required libraries are available.
     *
     * @return {@code true} if the libraries are available, {@code false} otherwise.
     */
    public static boolean areRequirementsAvailable() {
        return isOMEROGatewayInstalled()
               && isOMEROModelInstalled()
               && isOMEROBlitzInstalled()
               && isOMECommonInstalled()
               && isFormatsAPIInstalled();
    }


    /**
     * Checks if the omero-gateway is available.
     *
     * @return {@code true} if Gateway is available, {@code false} otherwise.
     */
    public static boolean isOMEROGatewayInstalled() {
        return checkClass("omero.gateway.Gateway");
    }


    /**
     * Checks if the omero-model is available.
     *
     * @return {@code true} if IObject is available, {@code false} otherwise.
     */
    public static boolean isOMEROModelInstalled() {
        return checkClass("omero.model.IObject");
    }


    /**
     * Checks if OMERO Blitz is available.
     *
     * @return {@code true} if OMEROMetadataStoreClient is available, {@code false} otherwise.
     */
    public static boolean isOMEROBlitzInstalled() {
        return checkClass("ome.formats.OMEROMetadataStoreClient");
    }


    /**
     * Checks if ome-common is available.
     *
     * @return {@code true} if DataTools is available, {@code false} otherwise.
     */
    public static boolean isOMECommonInstalled() {
        return checkClass("loci.common.DataTools");
    }


    /**
     * Checks if formats-api is available.
     *
     * @return {@code true} if DefaultMetadataOptions is available, {@code false} otherwise.
     */
    public static boolean isFormatsAPIInstalled() {
        return checkClass("loci.formats.in.DefaultMetadataOptions");
    }


    /**
     * Checks whether the given class is available.
     *
     * @param className The name of the class.
     *
     * @return {@code true} if the class was found, {@code false} otherwise.
     */
    public static boolean checkClass(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

}

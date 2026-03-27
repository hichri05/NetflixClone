package org.netflix.Utils;

import org.netflix.Models.Media;

public class TransferData {
    private static Media media;

    public static Media getMedia() {
        return media;
    }

    public static void setMedia(Media m) {
        media = m;
    }
}

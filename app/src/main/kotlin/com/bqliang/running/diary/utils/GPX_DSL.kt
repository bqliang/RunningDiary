package com.bqliang.running.diary.utils

import java.io.OutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

/**
 * Formats a [millis] timestamp to a string in the format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
 * @param millis the timestamp in milliseconds
 * @return the formatted timestamp
 */
fun isoFormatMillisSeconds(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    return formatter.format(instant)
}


/**
 * A Track Point holds the coordinates, elevation, timestamp, and metadata for a single point in a track.
 */
data class TrackPoint(
    private val latitude: Double,
    private val longitude: Double,
    private val elevation: Double,
    private val timeInMillsSeconds: Long
) {
    fun xml(): String =
        """
        <trkpt lat="$latitude" lon="$longitude">
            <ele>$elevation</ele>
            <time>${isoFormatMillisSeconds(timeInMillsSeconds)}</time>
        </trkpt>
        """.trimIndent()
}


/**
 * A Track Segment holds a list of [TrackPoint]s which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
 */
data class TrackSegment(private val trackPoints: MutableList<TrackPoint> = mutableListOf()) {

    fun trkpt(lat: Double, lon: Double, ele: Double, time: Long) {
        val trackPoint = TrackPoint(lat, lon, ele, time)
        trackPoints.add(trackPoint)
    }

    fun xml(): String =
        """
            <trkseg>
                ${trackPoints.joinToString("\n") { trackPoint -> trackPoint.xml() }}
            </trkseg>
        """.trimIndent()
}


/**
 * A Track is a list of Track Segments, which are logically connected in order. To represent a single GPS track where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span of track data.
 */
data class Track(
    private var type: String = "Running",
    private val extensions: MutableList<Pair<String, Any>> = mutableListOf(),
    private val trackSegments: MutableList<TrackSegment> = mutableListOf()
) {
    fun type(type: String) {
        this.type = type
    }

    fun extension(block: () -> Pair<String, Any>) {
        extensions.add(block())
    }

    fun trkseg(block: TrackSegment.() -> Unit) {
        val trackSegment = TrackSegment()
        trackSegment.block()
        trackSegments.add(trackSegment)
    }

    fun xml(): String =
        """
            <trk>
                <type>$type</type>
                <extensions>
                    ${extensions.joinToString("\n") { (key, value) -> "<$key>$value</$key>" }}
                </extensions>
                ${trackSegments.joinToString("\n") { segment -> segment.xml() }}
            </trk>
        """.trimIndent()
}


data class GPXFile(
    private var creator: String = "Running Diary",
    private val metadata: MutableList<Pair<String, Any>> = mutableListOf(),
    private val tracks: MutableList<Track> = mutableListOf()
) {
    fun creator(block: () -> String) {
        creator = block()
    }

    fun metadata(block: () -> Pair<String, Any>) {
        metadata.add(block())
    }

    fun trk(block: Track.() -> Unit) {
        val track = Track()
        track.block()
        tracks.add(track)
    }

    fun xml(): String =
        """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx 
                version="1.1" 
                creator="$creator"
                xmlns="http://www.topografix.com/GPX/1/1"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">
                <metadata>
                    ${metadata.joinToString("\n") { (key, value) -> "<$key>$value</$key>" }}
                </metadata>
                ${tracks.joinToString("\n") { track -> track.xml() }}
            </gpx>
        """.trimIndent()
            .lines()
            .joinToString("\n") { line -> line.trim() }

    fun toStream(outputStream: OutputStream) {
        outputStream.use { stream ->
            stream.write(xml().toByteArray())
        }
    }

}


fun gpx(block: GPXFile.() -> Unit): GPXFile {
    val gpxFile = GPXFile()
    gpxFile.block()
    return gpxFile
}
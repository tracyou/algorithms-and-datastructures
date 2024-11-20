package spotifycharts;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongComparatorTest {

    @Test
    public void testCompareByHighestStreamsCountTotal() {
        Song song1 = new Song("Artist1", "Title1", Song.Language.EN);
        Song song2 = new Song("Artist2", "Title2", Song.Language.NL);
        Song song3 = new Song("Artist3", "Title3", Song.Language.IT);

        song1.setStreamsCountOfCountry(Song.Country.UK, 10);
        song1.setStreamsCountOfCountry(Song.Country.NL, 20);
        song2.setStreamsCountOfCountry(Song.Country.UK, 15);
        song2.setStreamsCountOfCountry(Song.Country.NL, 25);
        song3.setStreamsCountOfCountry(Song.Country.UK, 5);
        song3.setStreamsCountOfCountry(Song.Country.NL, 15);

        // compare song1 with itself
        assertEquals(0, song1.compareByHighestStreamsCountTotal(song1));

        // compare song1 with song2 and song2 with song1
        int result1 = song1.compareByHighestStreamsCountTotal(song2);
        int result2 = song2.compareByHighestStreamsCountTotal(song1);
        assertEquals(-result1, result2);

        // compare song2 with song3 and song3 with song2
        result1 = song2.compareByHighestStreamsCountTotal(song3);
        result2 = song3.compareByHighestStreamsCountTotal(song2);
        assertEquals(-result1, result2);
    }

    @Test
    public void testCompareForDutchNationalChart() {
        Song song1 = new Song("Artist1", "Title1", Song.Language.EN);
        Song song2 = new Song("Artist2", "Title2", Song.Language.NL);
        Song song3 = new Song("Artist3", "Title3", Song.Language.IT);

        song1.setStreamsCountOfCountry(Song.Country.UK, 10);
        song1.setStreamsCountOfCountry(Song.Country.NL, 20);
        song2.setStreamsCountOfCountry(Song.Country.UK, 15);
        song2.setStreamsCountOfCountry(Song.Country.NL, 25);
        song3.setStreamsCountOfCountry(Song.Country.UK, 5);
        song3.setStreamsCountOfCountry(Song.Country.NL, 15);

        // compare song1 with itself
        assertEquals(0, song1.compareForDutchNationalChart(song1));

        // compare song1 with song2 and song2 with song1
        int result1 = song1.compareForDutchNationalChart(song2);
        int result2 = song2.compareForDutchNationalChart(song1);
        assertEquals(-result1, result2);

        // compare song2 with song3 and song3 with song2
        result1 = song2.compareForDutchNationalChart(song3);
        result2 = song3.compareForDutchNationalChart(song2);
        assertEquals(-result1, result2);
    }
}


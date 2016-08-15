package com.supermy.mongo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.Application;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class MongoDBGeoSpatialTest {

    private static final Point DUS = new Point( 6.810036, 51.224088 );

    @Autowired
    LocationRepository repo;

    @Autowired
    MongoTemplate template;

    @Before public void setUp() {

        // ensure geospatial index
        template.indexOps(Location.class).ensureIndex( new GeospatialIndex("position") );
        // prepare data
        repo.save( new Location("A", 0.001, -0.002) );
        repo.save( new Location("B", 1, 1) );
        repo.save( new Location("C", 0.5, 0.5) );
        repo.save( new Location("D", -0.5, -0.5) );

        repo.save( new Location("Berlin", 13.405838, 52.531261 ));
        repo.save( new Location("Cologne", 6.921272, 50.960157 ));
        repo.save( new Location("D�sseldorf", 6.810036, 51.224088 ) );
    }

    @Test public void shouldFindSelf() {
        // when
        List<Location> locations = repo.findByPositionNear(DUS , new Distance(1, Metrics.KILOMETERS) );

        // then
        assertLocations(locations, "D�sseldorf");
    }

    @Test public void shouldFindCologne() {
        // when
        List<Location> locations = repo.findByPositionNear(DUS , new Distance(70, Metrics.KILOMETERS) );

        // then
        assertLocations(locations, "D�sseldorf", "Cologne");
    }

    @Test public void shouldFindCologneAndBerlin() {
        // when
        List<Location> locations = repo.findByPositionNear(DUS , new Distance(350, Metrics.MILES) );

        // then
        assertLocations(locations, "D�sseldorf", "Cologne", "Berlin");
    }

    @Test public void shouldFindAll() {
        // when
        List<Location> locations = repo.findAll();

        // then
        assertLocations(locations, "A", "B", "C", "D", "Berlin", "Cologne", "D�sseldorf");
    }

    @Test public void shouldFindAroundOrigin() {
        // when
        List<Location> locations = repo.findByPositionWithin(new Circle(0, 0, 0.75));

        // then
        assertLocations(locations, "A", "C", "D");
    }

    @Test public void shouldFindWithinBox() {
        // when
//        List<Location> locations = repo.findByPositionWithin(new Box(new Point(0.25, 0.25), new Point(1, 1)));

        // then
//        assertLocations(locations, "B", "C");
    }

    @After
    public void tearDown() {
        repo.deleteAll();
    }

    private static void assertLocations(List<Location> locations, String... ids) {
        assertThat( locations, notNullValue() );
        out("-----------------------------");
        for (Location l : locations) {
            out(l);
        }
        assertThat("Mismatch location count", ids.length, is(locations.size()));
        for (String id : ids) {
            assertThat("Location " + id + " not found",
                    locations.contains(new Location(id, 0, 0)), is(true));
        }
    }

    private static void out(Object o) {
        System.out.println(o);
    }

}

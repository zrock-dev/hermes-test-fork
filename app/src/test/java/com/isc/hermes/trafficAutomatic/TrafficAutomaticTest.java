package com.isc.hermes.trafficAutomatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.isc.hermes.controller.CurrentLocationController;
import com.isc.hermes.controller.MapWayPointController;
import com.isc.hermes.controller.TrafficFormController;
import com.isc.hermes.controller.WaypointOptionsController;
import com.isc.hermes.database.TrafficUploader;
import com.isc.hermes.model.CurrentLocationModel;
import com.isc.hermes.model.graph.Graph;
import com.isc.hermes.model.graph.Node;
import com.isc.hermes.model.navigation.Route;
import com.isc.hermes.utils.DijkstraAlgorithm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TrafficAutomaticTest {
    MapWayPointController mapWayPointController;
    private  Context context;
    DijkstraAlgorithm dijkstraAlgorithm;
    TrafficUploader trafficUploader ;
    TrafficFormController trafficFormController = new TrafficFormController(context,mapWayPointController);

    @Before()
    public void setUp(){
        dijkstraAlgorithm = DijkstraAlgorithm.getInstance();
        trafficUploader = TrafficUploader.getInstance();
    }

    @Test
    public void getShortestPathWithRealStreetCoordinates(){
        Graph graph = new Graph();

        Node estebanArze_Sucre = new Node("estebanArze_Sucre", -17.394251, -66.156338);
        Node sucre_25mayo = new Node("sucre_25mayo", -17.394064, -66.155208);
        Node sucre_sanMartin = new Node("sucre_sanMartin", -17.393858, -66.154149);
        Node jordan_sanMartin = new Node("jordan_sanMartin", -17.394903, -66.153965);
        Node sucre_Lanza = new Node("sucre_Lanza", -17.393682, -66.153060);
        Node lanza_jordan = new Node("lanza_Jordan", -17.394716, -66.152910);


        estebanArze_Sucre.addUnidirectionalEdge(sucre_25mayo);
        sucre_25mayo.addUnidirectionalEdge(sucre_sanMartin);
        sucre_sanMartin.addUnidirectionalEdge(sucre_Lanza);
        sucre_Lanza.addUnidirectionalEdge(lanza_jordan);
        lanza_jordan.addUnidirectionalEdge(jordan_sanMartin);
        jordan_sanMartin.addUnidirectionalEdge(sucre_sanMartin);

        graph.addNode(estebanArze_Sucre);
        graph.addNode(sucre_25mayo);
        graph.addNode(sucre_sanMartin);
        graph.addNode(jordan_sanMartin);
        graph.addNode(sucre_Lanza);
        graph.addNode(lanza_jordan);

    }
    @Test
    public void testEstimateTimeHigh(){
        int stimate = trafficFormController.calculateEstimateTime(10,20);
        assertEquals(30,stimate);
    }
    @Test
    public void testEstimateTimeLow(){
        int stimate = trafficFormController.calculateEstimateTime(10,5);
        assertEquals(5,stimate);
    }
    @Test
    public void testEstimateTimeSame(){
        int stimate = trafficFormController.calculateEstimateTime(50,50);
        assertEquals(50,stimate);
    }

    @Test
    public void testTypeTrafficHigh(){
        String type = trafficFormController.getTrafficType(10,20);
        assertEquals("High Traffic",type);
    }
    @Test
    public void testTypeTrafficLow(){
        String type = trafficFormController.getTrafficType(20,5);
        assertEquals("Low Traffic",type);
    }
    @Test
    public void testTypeTrafficNormal(){
        String type = trafficFormController.getTrafficType(10,10);
        assertEquals("Normal Traffic",type);
    }
}

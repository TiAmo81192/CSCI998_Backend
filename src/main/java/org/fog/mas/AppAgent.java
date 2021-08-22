package org.fog.mas;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

public class AppAgent extends Agent{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static boolean CLOUD = false;
    //static List<Task> tasks = new ArrayList<Task>();

    public void setup() {
        try {

            System.out.println("Application Agent start...");
            String appId = "APP"+getAID().getLocalName();
            String brokerName = "broker"+getAID().getLocalName();

            FogBroker broker = new FogBroker(brokerName); //the user of the application
            String deviceId = "d-"+getAID().getLocalName();

            FogDevice device = addDevice(deviceId, broker.getId(), appId, 3);
            device.setUplinkLatency(2);

            MainClass.fogDevices.add(device);

            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            System.out.println("The create application name is " + appId);

            //Controller controller = null; // initialize the controller
            //System.out.println("Initialize the controller!");

            //Calendar currentTime = Calendar.getInstance();
            //currentTime.add(Calendar.SECOND, 150);
            //Date dt = currentTime.getTime();//deadline of the task
            //Task task = new Task(application.getModuleByName(appId), dt);

            //doWait(2000);


            //print the tasks list
            //ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();//initializing a module mapping

            Map m = new Map(appId, MainClass.fogDevices.get(1).getName(), application);

            MainClass.maps.add(m);
            //moduleMapping.addModuleToDevice(appId, MainClass.fogDevices.get(1).getName());

            //String controllerName = "master-controller"+getAID().getLocalName();
            //Controller controller = new Controller(controllerName, MainClass.fogDevices, MainClass.sensors, MainClass.actuators);

            //controller.submitApplication(application, 100, new ModulePlacementMapping(MainClass.fogDevices, application, moduleMapping));

        }
        catch(Exception e) {
            System.out.println("Application "+getAID().getLocalName()+" Agent Error");

        }

    }

    @SuppressWarnings({"serial" })
    private static Application createApplication(String appId, int userId){

        Application application = Application.createApplication(appId, userId);
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */

        application.addAppModule(appId, 10); //the module name is online_resource and the ram requirement is 100

        System.out.println("Set the application module success!");

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */

        String sensorName = "EEG"+appId;
        String actName = "DISPLAY"+appId;
        //String tupleType = "Update"+appId;

        application.addAppEdge(sensorName, appId, 1000, 2000, sensorName, Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge(appId, actName, 100, 200, actName, Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping(appId, sensorName, actName, new FractionalSelectivity(1));
		/*
		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{add(sensorName);add(appId);add(actName);}});

		List<AppLoop> loops = new ArrayList<AppLoop>() {

			{
		     add(loop1);
	}

		};
		application.setLoops(loops);
		*/
        System.out.println("Set the application loop success!");
        return application;
    }

    private static FogDevice addDevice(String id, int userId, String appId, int parentId) {
        FogDevice device = createFogDevice("d-"+id, 1000, 1000, 10000, 270, 3, 0, 87.53, 82.44);
        device.setParentId(parentId);
        MainClass.devices.add(device);
        Sensor sensor = new Sensor("s-"+appId+id, "EEG"+appId, userId, appId, new DeterministicDistribution(5));
        MainClass.sensors.add(sensor);
        Actuator actuator =  new Actuator("a-"+appId+id, userId, appId, "DISPLAY"+appId);
        MainClass.actuators.add(actuator);
        sensor.setGatewayDeviceId(device.getId());
        sensor.setLatency(6.0);
        actuator.setGatewayDeviceId(device.getId());
        actuator.setLatency(1.0);
        return device;
    }

    private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw,
                                             int level, double ratePerMips, double busyPower, double idlePower) {
        List<Pe> peList = new ArrayList<Pe>();

        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); //create Processing Element and added these into a list
        //store the Pe id and MIPS rating

        int hostId = FogUtils.generateEntityId();//set host Id
        long storage = 50000; //set host storage
        int bw = 3000; // set host bandwidth

        //set the power host
        PowerHost host = new PowerHost(hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86";//system architecture
        String os = "Linux";//operating system
        String vmm = "Xen";
        double time_zone = 10.0;//time zone this resource located
        double cost = 3.0; //the cost of using processing in this resource
        double costPerMem = 0.05; //the cost of using memory in this resource
        double costPerStorage = 0.003; //the cost of using storage in this resource
        double costPerBw = 0.01;//the cost of using bandwidth in this resource

        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        //(storage area network) devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        //set the fog device characteristics

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics, new AppModuleAllocationPolicy(hostList),
                    storageList, 10, upBw,downBw, 0, ratePerMips);

        }catch(Exception e) {
            e.printStackTrace();
        }
        fogdevice.setLevel(level);
        return fogdevice;
    }

}

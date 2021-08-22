package agent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.Runtime;
public class MainClass {

    static List<Datacenter> datacenters = new ArrayList<Datacenter>();
//    static List<Sensor> sensors = new ArrayList<Sensor>();
//    static List<Actuator> actuators = new ArrayList<Actuator>();
//    static List<FogDevice> devices = new ArrayList<FogDevice>();
//    static List<Application> applications = new ArrayList<Application>();
//    static List<Map> maps = new ArrayList<Map>();

    /** The cloudlet lists. */
    private static List<Cloudlet> cloudletList1;
    private static List<Cloudlet> cloudletList2;

    /** The vmlists. */
    private static List<Vm> vmlist1;
    private static List<Vm> vmlist2;

    static boolean CLOUD = false;

    public static void main(String[] args) {

        try{
            //set the Multi-Agent System Environment
            Log.printLine("Start the run time...");
            Runtime runTime = Runtime.instance();
            runTime.setCloseVM(true);//exit the JVM when there is no container around
            Profile profileMain=new ProfileImpl(null,8888,null);//set the profile to start the container
            System.out.println("Launching a whole in-process platform"+profileMain);
            AgentContainer mainContainer=runTime.createMainContainer(profileMain);//create the container

            //Initialize the Cloud-Fog environment
            System.out.println("Initialize the fog-cloud environment");
            Log.disable();
            int num_user=1; //number of cloud users
            Calendar calendar=Calendar.getInstance();//set the system time calendar
            boolean trace_flag=false;//mean trace event

            CloudSim.init(num_user, calendar, trace_flag);

            //create the cloud
            for(int z =2000,len=2001; z<len;z++) {
                String agentName = String.valueOf(z);
                AgentController createAgent = mainContainer.createNewAgent(agentName,
                        "agent.CloudAgent", null);
                createAgent.start();
            }

            Thread.sleep(2000);//wait one second
            //create fog devices and fog agents
            for(int i=0,len=2; i<len;i++) {
                String agentName = String.valueOf(i);
                AgentController createAgent = mainContainer.createNewAgent(agentName,
                        "agent.FogAgent", null);
                createAgent.start();
                Thread.sleep(100);
            }

            Thread.sleep(2000);//wait one second
            //create applications
            for(int j=1000,len=1005; j<len;j++) {
                String agentName = String.valueOf(j);
                AgentController createAgent = mainContainer.createNewAgent(agentName,
                        "agent.AppAgent", null);
                createAgent.start();
                Thread.sleep(2000);
            }

//            System.out.println("submit application!");
//            //Thread.sleep(2000);
//            for(int i=0,len=vmlist1.size(); i<len;i++) {
//                System.out.println("Fog devices are: "+vmlist1.get(i).getName()+"...");
//            }

//            Thread.sleep(2000);
//            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
//
//            Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);
//
//
//            for(Map m : maps) {
//                String appId = m.getAppId();
//                String nodeName = m.getNodeName();
//                Application app = m.getApp();
//                System.out.println("The appId is "+appId+", the node name is "+nodeName+", the app's id is "+app.getAppId());
//                ModuleMapping mp = ModuleMapping.createModuleMapping();
//                mp.addModuleToDevice(appId, nodeName);
//                controller.submitApplication(app, new ModulePlacementMapping(fogDevices, app, mp));
//            }

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

        }catch(Exception e) {
            System.out.println("Main Class Error");
        }
    }
}

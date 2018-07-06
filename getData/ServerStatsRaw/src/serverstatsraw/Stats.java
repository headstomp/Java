/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverstatsraw;

/**
 *
 * @author Sandeep.Gainda
 */
public enum Stats
{

    CPU_UTILIZATION(".CPU.Utilization", "CPU Utilization Average On All Cores (%)", ":5693/api/cpu/percent/?token=isthefirereal"),
    RAM_UTILIZATION(".RAM.Utilization", "RAM Utilization (%)", ":5693/api/memory/virtual/percent/?token=isthefirereal"),
    RAM_USED(".RAM.Used", "RAM Used (MB)", ":5693/api/memory/virtual/used/?token=isthefirereal"),
    CDISK_UTILIZATION(".Disk.Utilization", "C Drive Disk Utilization (%)", ":5693/api/disk/logical/C:%7C/used_percent/?token=isthefirereal"),
    CDISK_USED(".Disk.Used", "C Drive Disk Used (MB)", ":5693/api/disk/logical/C:%7C/used/?token=isthefirereal"),
    NET_SENT(".Network.Sent", "Ethernet 0 data sent (MB)", ":5693/api/interface/Ethernet0/bytes_sent/?token=isthefirereal"),
    NET_REC(".Network.Received", "Ethernet 0 data received (MB)", ":5693/api/interface/Ethernet0/bytes_recv/?token=isthefirereal"),
    UP_TIME(".Up.Time", "Host Uptime (Days)", ":5693/api/system/uptime?token=isthefirereal");

    public final String aspect;
    public final String description;
    public final String api;

    private Stats(String aspect, String description, String api)
    {
        this.aspect = aspect;
        this.description = description;
        this.api = api;
    }

}

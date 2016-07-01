package fr.rsommerard.privacyaware.wifidirect.device;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.DaoMaster;
import fr.rsommerard.privacyaware.dao.DaoMaster.DevOpenHelper;
import fr.rsommerard.privacyaware.dao.DaoSession;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.dao.Device;
import fr.rsommerard.privacyaware.dao.DeviceDao;

public class DeviceManager {

    private static final int AVAILABILITY = 120000;

    private final SecureRandom mRandom;
    private final DeviceDao mDeviceDao;
    private final EventBus mEventBus;

    public DeviceManager(final Context context, final EventBus eventBus) {
        DevOpenHelper helper = new DevOpenHelper(context, "privacy-aware-db", null);
        SQLiteDatabase mDb = helper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(mDb);
        DaoSession mDaoSession = mDaoMaster.newSession();
        mDeviceDao = mDaoSession.getDeviceDao();

        mDeviceDao.deleteAll(); // TODO: Delete this line

        mRandom = new SecureRandom();

        mEventBus = eventBus;
        mEventBus.register(this);
    }

    @Subscribe
    public void onDeviceEvent(final DeviceEvent event) {
        if (containsDevice(event.device)) {
            updateDevice(event.device);
        } else {
            addDevice(event.device);
        }
    }

    public Device getDevice() {
        List<Device> devices = mDeviceDao.loadAll();
        return devices.get(mRandom.nextInt(devices.size()));
    }

    public Device getDevice(final Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        return query.unique();
    }

    public List<Device> getAllDevices() {
        return mDeviceDao.loadAll();
    }

    public boolean hasDevices() {
        long limit = System.currentTimeMillis() - AVAILABILITY;
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Timestamp.gt(limit));

        Query<Device> query = qBuilder.build();

        return query.list().size() != 0;
    }

    private boolean containsDevice(final Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        return query.unique() != null;
    }

    private void updateDevice(final Device device) {
        QueryBuilder<Device> qBuilder = mDeviceDao.queryBuilder();
        qBuilder.where(DeviceDao.Properties.Address.eq(device.getAddress()));

        Query<Device> query = qBuilder.build();

        Device d = query.unique();
        device.setId(d.getId());

        mDeviceDao.update(device);
        Log.i(WiFiDirect.TAG, "Update " + d.toString() + " to " + device.toString());
    }

    private void addDevice(final Device device) {
        mDeviceDao.insert(device);
        Log.i(WiFiDirect.TAG, "Insert " + device.toString());
    }

    public void deleteAll() {
        mDeviceDao.deleteAll();
    }
}
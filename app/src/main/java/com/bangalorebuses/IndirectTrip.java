package com.bangalorebuses;

import java.util.ArrayList;

class IndirectTrip extends Trip
{
    private ArrayList<BusRoute> busRoutesOnLeg1 = new ArrayList<>();
    private ArrayList<BusRoute> busRoutesOnLeg2 = new ArrayList<>();

    private TransitPoint transitPoint;
    private int transitPointScore;

    private int numberOfStopsOnLeg1;
    private int numberOfStopsOnLeg2;

    @Override
    public void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder)
    {

    }

    public ArrayList<BusRoute> getBusRoutesOnLeg1()
    {
        return busRoutesOnLeg1;
    }

    public void setBusRoutesOnLeg1(ArrayList<BusRoute> busRoutesOnLeg1)
    {
        this.busRoutesOnLeg1 = busRoutesOnLeg1;
    }

    public void addBusRouteToLeg1(BusRoute busRoute)
    {
        busRoutesOnLeg1.add(busRoute);
    }

    public void clearBusRoutesOnLeg1()
    {
        busRoutesOnLeg1.clear();
    }

    public ArrayList<BusRoute> getBusRoutesOnLeg2()
    {
        return busRoutesOnLeg2;
    }

    public void setBusRoutesOnLeg2(ArrayList<BusRoute> busRoutesOnLeg2)
    {
        this.busRoutesOnLeg2 = busRoutesOnLeg2;
    }

    public void addBusRouteToLeg2(BusRoute busRoute)
    {
        busRoutesOnLeg2.add(busRoute);
    }

    public void clearBusRoutesOnLeg2()
    {
        busRoutesOnLeg2.clear();
    }

    public TransitPoint getTransitPoint()
    {
        return transitPoint;
    }

    public void setTransitPoint(TransitPoint transitPoint)
    {
        this.transitPoint = transitPoint;
    }

    public int getTransitPointScore()
    {
        return transitPointScore;
    }

    public void setTransitPointScore(int transitPointScore)
    {
        this.transitPointScore = transitPointScore;
    }

    public int getNumberOfStopsOnLeg1()
    {
        return numberOfStopsOnLeg1;
    }

    public void setNumberOfStopsOnLeg1(int numberOfStopsOnLeg1)
    {
        this.numberOfStopsOnLeg1 = numberOfStopsOnLeg1;
    }

    public int getNumberOfStopsOnLeg2()
    {
        return numberOfStopsOnLeg2;
    }

    public void setNumberOfStopsOnLeg2(int numberOfStopsOnLeg2)
    {
        this.numberOfStopsOnLeg2 = numberOfStopsOnLeg2;
    }
}
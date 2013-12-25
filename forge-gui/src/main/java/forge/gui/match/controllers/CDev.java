package forge.gui.match.controllers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import forge.Command;
import forge.Singletons;
import forge.game.player.Player;
import forge.gui.GuiDisplayUtil;
import forge.gui.framework.ICDoc;
import forge.gui.match.views.VDev;
import forge.net.FServer;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;

/**
 * Controls the combat panel in the match UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CDev implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

//    //========= Start mouse listener inits
//    private final MouseListener madMilling = new MouseAdapter() { @Override
//        public void mousePressed(final MouseEvent e) {
//        toggleLossByMilling();
//    } };
//    public void toggleLossByMilling() {
//        VDev.SINGLETON_INSTANCE.getLblMilling().toggleEnabled();
//        Singletons.getModel().getPreferences().writeMatchPreferences();
//        Singletons.getModel().getPreferences().save();
//    }

    private final MouseListener madUnlimited = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        togglePlayManyLandsPerTurn();
    } };
    public void togglePlayManyLandsPerTurn() {
        VDev.SINGLETON_INSTANCE.getLblUnlimitedLands().toggleEnabled();
        boolean newValue = VDev.SINGLETON_INSTANCE.getLblUnlimitedLands().getEnabled();
        Singletons.getModel().getPreferences().setPref(FPref.DEV_UNLIMITED_LAND, String.valueOf(newValue));
        
        for(Player p : Singletons.getControl().getObservedGame().getPlayers()) {
            if( p.getLobbyPlayer() == FServer.instance.getLobby().getGuiPlayer() )
                p.canCheatPlayUnlimitedLands = newValue;
        }
        // probably will need to call a synchronized method to have the game thread see changed value of the variable 
        
        Singletons.getModel().getPreferences().save();
    }

    private final MouseListener madMana = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        generateMana(); }
    };
    public void generateMana() {
        GuiDisplayUtil.devModeGenerateMana();
        Singletons.getModel().getPreferences().save();
    }

    private final MouseListener madSetup = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        setupGameState(); }
    };
    public void setupGameState() {
        GuiDisplayUtil.devSetupGameState();
    }

    private final MouseListener madTutor = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        tutorForCard(); }
    };
    public void tutorForCard() {
        GuiDisplayUtil.devModeTutor();
    }

    private final MouseListener madCardToHand = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        addCardToHand(); }
    };
    public void addCardToHand() {
        GuiDisplayUtil.devModeCardToHand();
    }

    private final MouseListener madCounter = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        addCounterToPermanent(); }
    };
    public void addCounterToPermanent() {
        GuiDisplayUtil.devModeAddCounter();
    }

    private final MouseListener madTap = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        tapPermanent(); }
    };
    public void tapPermanent() {
        GuiDisplayUtil.devModeTapPerm();
    }

    private final MouseListener madUntap = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        untapPermanent(); }
    };
    public void untapPermanent() {
        GuiDisplayUtil.devModeUntapPerm();
    }

    private final MouseListener madLife = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        setPlayerLife(); }
    };
    public void setPlayerLife() {
        GuiDisplayUtil.devModeSetLife();
    }

    private final MouseListener madCardToBattlefield = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        addCardToPlay(); }
    };
    public void addCardToPlay() {
        GuiDisplayUtil.devModeCardToBattlefield();
    }

    private final MouseListener madRiggedRoll = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        riggedPlanerRoll(); }
    };
    public void riggedPlanerRoll() {
        GuiDisplayUtil.devModeRiggedPlanarRoll();
    }

    private final MouseListener madWalkToPlane = new MouseAdapter() { @Override
        public void mousePressed(final MouseEvent e) {
        planeswalkTo(); }
    };
    public void planeswalkTo() {
        GuiDisplayUtil.devModePlaneswalkTo();
    }

    //========== End mouse listener inits

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    public void initialize() {
       //  VDev.SINGLETON_INSTANCE.getLblMilling().addMouseListener(madMilling);
        VDev.SINGLETON_INSTANCE.getLblUnlimitedLands().addMouseListener(madUnlimited);
        VDev.SINGLETON_INSTANCE.getLblGenerateMana().addMouseListener(madMana);
        VDev.SINGLETON_INSTANCE.getLblSetupGame().addMouseListener(madSetup);
        VDev.SINGLETON_INSTANCE.getLblTutor().addMouseListener(madTutor);
        VDev.SINGLETON_INSTANCE.getLblCardToHand().addMouseListener(madCardToHand);
        VDev.SINGLETON_INSTANCE.getLblCounterPermanent().addMouseListener(madCounter);
        VDev.SINGLETON_INSTANCE.getLblTapPermanent().addMouseListener(madTap);
        VDev.SINGLETON_INSTANCE.getLblUntapPermanent().addMouseListener(madUntap);
        VDev.SINGLETON_INSTANCE.getLblSetLife().addMouseListener(madLife);
        VDev.SINGLETON_INSTANCE.getLblCardToBattlefield().addMouseListener(madCardToBattlefield);
        VDev.SINGLETON_INSTANCE.getLblRiggedRoll().addMouseListener(madRiggedRoll);
        VDev.SINGLETON_INSTANCE.getLblWalkTo().addMouseListener(madWalkToPlane);

        ForgePreferences prefs = Singletons.getModel().getPreferences();

       //  VDev.SINGLETON_INSTANCE.getLblMilling().setEnabled(prefs.getPrefBoolean(FPref.DEV_MILLING_LOSS));
        //VDev.SINGLETON_INSTANCE.getLblMilling().setEnabled(Constant.Runtime.MILL[0]);
        VDev.SINGLETON_INSTANCE.getLblUnlimitedLands().setEnabled(prefs.getPrefBoolean(FPref.DEV_UNLIMITED_LAND));
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        // VDev.SINGLETON_INSTANCE.getLblMilling().setEnabled(Singletons.getModel().getPreferences().getPrefBoolean(FPref.DEV_MILLING_LOSS));
        VDev.SINGLETON_INSTANCE.getLblUnlimitedLands().setEnabled(Singletons.getModel().getPreferences().getPrefBoolean(FPref.DEV_UNLIMITED_LAND));
    }

}

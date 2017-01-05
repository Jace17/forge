package forge.ai.ability;

import com.google.common.base.Predicates;
import forge.ai.ComputerUtil;
import forge.ai.ComputerUtilCost;
import forge.ai.ComputerUtilMana;
import forge.ai.SpellAbilityAi;
import forge.card.CardType.Supertype;
import forge.card.mana.ManaCost;
import forge.game.Game;
import forge.game.GlobalRuleChange;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.cost.Cost;
import forge.game.mana.ManaCostBeingPaid;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import org.apache.commons.lang3.StringUtils;

public class PermanentAi extends SpellAbilityAi {

    /**
     * Checks if the AI will play a SpellAbility based on its phase restrictions
     */
    @Override
    protected boolean checkPhaseRestrictions(final Player ai, final SpellAbility sa, final PhaseHandler ph) {

        final Card card = sa.getHostCard();

        if (card.hasStartOfKeyword("You may cast CARDNAME as though it had flash. If") && !ai.couldCastSorcery(sa)) {
            // AiPlayDecision.AnotherTime
            return false;
        }

        // Wait for Main2 if possible
        if (ph.is(PhaseType.MAIN1) && ph.isPlayerTurn(ai) && !ComputerUtil.castPermanentInMain1(ai, sa)) {
            return false;
        }
        return true;
    }

    /**
     * The rest of the logic not covered by the canPlayAI template is defined
     * here
     */
    @Override
    protected boolean checkApiLogic(final Player ai, final SpellAbility sa) {

        final Card card = sa.getHostCard();
        final Game game = ai.getGame();

        // check on legendary
        if (card.getType().isLegendary()
                && !game.getStaticEffects().getGlobalRuleChange(GlobalRuleChange.noLegendRule)) {
            if (ai.isCardInPlay(card.getName())) {
                // AiPlayDecision.WouldDestroyLegend
                return false;
            }
        }
        if (card.isPlaneswalker()) {
            CardCollection list = CardLists.filter(ai.getCardsIn(ZoneType.Battlefield),
                    CardPredicates.Presets.PLANEWALKERS);
            for (String type : card.getType().getSubtypes()) { // determine
                                                               // planewalker
                                                               // subtype
                final CardCollection cl = CardLists.getType(list, type);
                if (!cl.isEmpty()) {
                    // AiPlayDecision.WouldDestroyOtherPlaneswalker
                    return false;
                }
                break;
            }
        }

        if (card.getType().hasSupertype(Supertype.World)) {
            CardCollection list = CardLists.getType(ai.getCardsIn(ZoneType.Battlefield), "World");
            if (!list.isEmpty()) {
                // AiPlayDecision.WouldDestroyWorldEnchantment
                return false;
            }
        }

        ManaCost mana = sa.getPayCosts().getTotalMana();
        if (mana.countX() > 0) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
            final Card source = sa.getHostCard();
            if (source.hasConverge()) {
                card.setSVar("PayX", Integer.toString(0));
                int nColors = ComputerUtilMana.getConvergeCount(sa, ai);
                for (int i = 1; i <= xPay; i++) {
                    card.setSVar("PayX", Integer.toString(i));
                    int newColors = ComputerUtilMana.getConvergeCount(sa, ai);
                    if (newColors > nColors) {
                        nColors = newColors;
                    } else {
                        card.setSVar("PayX", Integer.toString(i - 1));
                        break;
                    }
                }
            } else {
                // AiPlayDecision.CantAffordX
                if (xPay <= 0) {
                    return false;
                }
                card.setSVar("PayX", Integer.toString(xPay));
            }
        } else if (mana.isZero()) {
            // if mana is zero, but card mana cost does have X, then something
            // is wrong
            ManaCost cardCost = card.getManaCost();
            if (cardCost != null && cardCost.countX() > 0) {
                // AiPlayDecision.CantPlayAi
                return false;
            }
        }

        if (sa.hasParam("Announce") && sa.getParam("Announce").startsWith("Multikicker")) {
            // String announce = sa.getParam("Announce");
            ManaCost mkCost = sa.getMultiKickerManaCost();
            ManaCost mCost = sa.getPayCosts().getTotalMana();
            for (int i = 0; i < 10; i++) {
                mCost = ManaCost.combine(mCost, mkCost);
                ManaCostBeingPaid mcbp = new ManaCostBeingPaid(mCost);
                if (!ComputerUtilMana.canPayManaCost(mcbp, sa, ai)) {
                    card.setKickerMagnitude(i);
                    break;
                }
                card.setKickerMagnitude(i + 1);
            }
        }

        // don't play cards without being able to pay the upkeep for
        for (String ability : card.getKeywords()) {
            if (ability.startsWith("UpkeepCost")) {
                final String[] k = ability.split(":");
                final String costs = k[1];

                final SpellAbility emptyAbility = new SpellAbility.EmptySa(card, ai);
                emptyAbility.setPayCosts(new Cost(costs, true));
                emptyAbility.setTargetRestrictions(sa.getTargetRestrictions());

                emptyAbility.setActivatingPlayer(ai);
                if (!ComputerUtilCost.canPayCost(emptyAbility, ai)) {
                    // AiPlayDecision.AnotherTime
                    return false;
                }
            }
        }

        // check for specific AI preferences
        if (card.hasSVar("AICastPreference")) {
            String pref = card.getSVar("AICastPreference");
            String[] groups = StringUtils.split(pref, "|");
            for (String group : groups) {
                String[] elems = StringUtils.split(group.trim(), '$');
                String param = elems[0].trim();
                String value = elems[1].trim();
                if (param.equals("MustHaveInHand")) {
                    // Only cast if another card is present in hand (e.g. Illusions of Grandeur followed by Donate)
                    boolean hasCard = CardLists.filter(ai.getCardsIn(ZoneType.Hand), CardPredicates.nameEquals(value)).size() > 0;
                    if (!hasCard) {
                        return false;
                    }
                } else if (param.equals("MaxControlled")) {
                    // Only cast unless there are X or more cards like this on the battlefield under AI control already
                    int numControlled = CardLists.filter(ai.getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals(card.getName())).size();
                    if (numControlled >= Integer.parseInt(value)) {
                        return false;
                    }
                } else if (elems[0].trim().equals("NumManaSources")) {
                    // Only cast if there are X or more mana sources controlled by the AI
                    CardCollection m = ComputerUtilMana.getAvailableMana(ai, true);
                    if (m.size() < Integer.parseInt(value)) {
                        return false;
                    }
                } else if (elems[0].trim().equals("NumManaSourcesNextTurn")) {
                    // Only cast if there are X or more mana sources controlled by the AI *or*
                    // if there are X-1 mana sources in play but the AI has an extra land in hand
                    CardCollection m = ComputerUtilMana.getAvailableMana(ai, true);
                    int extraMana = CardLists.filter(ai.getCardsIn(ZoneType.Hand), CardPredicates.Presets.LANDS).size() > 0 ? 1 : 0;
                    if (card.getName().equals("Illusions of Grandeur")) {
                        // TODO: this is currently hardcoded for specific Illusions-Donate cost reduction spells, need to make this generic.
                       extraMana += Math.min(3, CardLists.filter(ai.getCardsIn(ZoneType.Battlefield), Predicates.or(CardPredicates.nameEquals("Sapphire Medallion"), CardPredicates.nameEquals("Helm of Awakening"))).size()) * 2; // each cost-reduction spell accounts for {1} in both Illusions and Donate
                    }
                    if (m.size() + extraMana < Integer.parseInt(value)) {
                        return false;
                    }
                }
            }
            
        }
        
        return true;
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final Card source = sa.getHostCard();
        final Cost cost = sa.getPayCosts();

        if (sa.getConditions() != null && !sa.getConditions().areMet(sa)) {
            return false;
        }

        if (sa.hasParam("AILogic") && !checkAiLogic(ai, sa, sa.getParam("AILogic"))) {
            return false;
        }
        if (cost != null && !willPayCosts(ai, sa, cost, source)) {
            return false;
        }
        if (!checkPhaseRestrictions(ai, sa, ai.getGame().getPhaseHandler())) {
            return false;
        }
        return checkApiLogic(ai, sa);
    }

}

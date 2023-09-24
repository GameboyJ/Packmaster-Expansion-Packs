package thePackmaster.cards.pickthemallpack;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.actions.tempHp.AddTemporaryHPAction;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.OnObtainCard;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import thePackmaster.SpireAnniversary5Mod;

public class BloodWall extends AbstractPickThemAllCard implements OnObtainCard {
    public static final String ID = SpireAnniversary5Mod.makeID("BloodWall");
    private static final int COST = 1;
    private static final int BLOCK = 4;
    private static final int UPGRADE_BLOCK = 1;
    private static final int TEMP_HP = 5;
    private static final int UPGRADE_TEMP_HP = 2;
    private static final int PICKUP_HP_LOSS = 4;

    public BloodWall() {
        super(ID, COST, CardType.SKILL, CardRarity.COMMON, CardTarget.SELF);
        this.baseBlock = BLOCK;
        this.magicNumber = this.baseMagicNumber = TEMP_HP;
        this.secondMagic = this.baseSecondMagic = PICKUP_HP_LOSS;
    }

    @Override
    public void upp() {
        this.upgradeBlock(UPGRADE_BLOCK);
        this.upgradeMagicNumber(UPGRADE_TEMP_HP);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        this.addToBot(new GainBlockAction(p, this.block));
        this.addToBot(new AddTemporaryHPAction(p, p, this.magicNumber));
    }

    @Override
    public void onObtainCard() {
        // We want to avoid negative scenarios like getting this from Astrolabe or Pandora's Box at low enough HP that
        // it would kill the player, so we make this not reduce the player below 1 HP
        int hpLoss = Math.min(PICKUP_HP_LOSS, AbstractDungeon.player.currentHealth);
        if (hpLoss > 0) {
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                this.addToBot(new DamageAction(AbstractDungeon.player, new DamageInfo(AbstractDungeon.player, hpLoss, DamageInfo.DamageType.HP_LOSS)));
            }
            else {
                AbstractDungeon.topLevelEffectsQueue.add(new AbstractGameEffect() {
                    @Override
                    public void update() {
                        AbstractDungeon.player.damage(new DamageInfo(null, hpLoss, DamageInfo.DamageType.HP_LOSS));
                        this.isDone = true;
                    }
                    @Override
                    public void render(SpriteBatch spriteBatch) {}
                    @Override
                    public void dispose() {}
                });
            }
        }
    }
}

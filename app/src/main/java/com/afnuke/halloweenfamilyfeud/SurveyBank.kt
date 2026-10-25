package com.afnuke.halloweenfamilyfeud

data class SurveyAnswer(val text: String, val points: Int)
data class SurveyRound(val question: String, val answers: List<SurveyAnswer>)

val surveyBank = listOf(
    SurveyRound("Name something people dress up as for Halloween", listOf(
        SurveyAnswer("Witch", 32), SurveyAnswer("Vampire/Dracula", 22),
        SurveyAnswer("Ghost", 14), SurveyAnswer("Zombie", 10),
        SurveyAnswer("Pirate", 8), SurveyAnswer("Clown", 6),
        SurveyAnswer("Superhero", 5), SurveyAnswer("Cat", 3))),
    SurveyRound("Name a popular Halloween candy", listOf(
        SurveyAnswer("Candy corn", 26), SurveyAnswer("Snickers", 18),
        SurveyAnswer("Reese's Peanut Butter Cups", 14), SurveyAnswer("M&M's", 12),
        SurveyAnswer("Skittles", 10), SurveyAnswer("Kit Kat", 8),
        SurveyAnswer("Lollipops", 7), SurveyAnswer("Tootsie Rolls", 5))),
    SurveyRound("Name something scary you'd find in a haunted house", listOf(
        SurveyAnswer("Ghosts", 30), SurveyAnswer("Cobwebs", 18),
        SurveyAnswer("Skeletons", 16), SurveyAnswer("Spiders", 12),
        SurveyAnswer("Bats", 9), SurveyAnswer("Coffin", 7),
        SurveyAnswer("Creepy noises", 5), SurveyAnswer("Witch", 3))),
    SurveyRound("Name a Halloween movie", listOf(
        SurveyAnswer("Hocus Pocus", 28), SurveyAnswer("Halloween", 24),
        SurveyAnswer("The Nightmare Before Christmas", 14), SurveyAnswer("Ghostbusters", 12),
        SurveyAnswer("The Addams Family", 8), SurveyAnswer("Beetlejuice", 6),
        SurveyAnswer("Casper", 5), SurveyAnswer("Scream", 3))),
    SurveyRound("Name something that scares a vampire", listOf(
        SurveyAnswer("Sunlight", 36), SurveyAnswer("Garlic", 26),
        SurveyAnswer("Wooden stake", 16), SurveyAnswer("Cross", 10),
        SurveyAnswer("Holy water", 5), SurveyAnswer("Mirror", 3),
        SurveyAnswer("Silver", 2), SurveyAnswer("Werewolf", 2))),
    SurveyRound("Name something a witch carries or keeps", listOf(
        SurveyAnswer("Broom", 50), SurveyAnswer("Black cat", 14),
        SurveyAnswer("Spell book", 10), SurveyAnswer("Wand", 8),
        SurveyAnswer("Cauldron", 7), SurveyAnswer("Pointy hat", 5),
        SurveyAnswer("Crystal ball", 4), SurveyAnswer("Potion bottle", 2))),
    SurveyRound("Name a place monsters like to hide", listOf(
        SurveyAnswer("Under the bed", 32), SurveyAnswer("Closet", 24),
        SurveyAnswer("Basement", 16), SurveyAnswer("Attic", 10),
        SurveyAnswer("Behind the door", 7), SurveyAnswer("Under the stairs", 5),
        SurveyAnswer("Graveyard", 4), SurveyAnswer("Woods", 2))),
    SurveyRound("Name a game people play at Halloween parties", listOf(
        SurveyAnswer("Bobbing for apples", 42), SurveyAnswer("Costume contest", 20),
        SurveyAnswer("Mummy wrap race", 10), SurveyAnswer("Pin the hat on the witch", 8),
        SurveyAnswer("Scary movie marathon", 7), SurveyAnswer("Pumpkin carving contest", 6),
        SurveyAnswer("Ghost stories", 4), SurveyAnswer("Candy hunt", 3))),
    SurveyRound("Name something that glows on Halloween night", listOf(
        SurveyAnswer("Jack-o'-lantern", 44), SurveyAnswer("Ghost decorations", 14),
        SurveyAnswer("Glow sticks", 12), SurveyAnswer("Full moon", 10),
        SurveyAnswer("Skeleton bones", 8), SurveyAnswer("Porch lights", 5),
        SurveyAnswer("Black light", 4), SurveyAnswer("Fireflies", 3))),
    SurveyRound("Name a trick-or-treat safety rule", listOf(
        SurveyAnswer("Say thank you", 22), SurveyAnswer("Don't go alone", 20),
        SurveyAnswer("Check your candy", 18), SurveyAnswer("Only visit lit houses", 12),
        SurveyAnswer("Stay on the sidewalk", 10), SurveyAnswer("Wear something reflective", 8),
        SurveyAnswer("Don't eat unwrapped candy", 6), SurveyAnswer("Be home by curfew", 4))),
    SurveyRound("Name something a witch drops in her cauldron", listOf(
        SurveyAnswer("Eye of newt", 22), SurveyAnswer("Frog legs", 16),
        SurveyAnswer("Bat wings", 14), SurveyAnswer("Spider legs", 12),
        SurveyAnswer("Lizard tail", 10), SurveyAnswer("Snake skin", 9),
        SurveyAnswer("Worms", 9), SurveyAnswer("Mystery slime", 8))),
    SurveyRound("Name a place you'd never want to spend Halloween night", listOf(
        SurveyAnswer("Haunted house", 38), SurveyAnswer("Graveyard", 22),
        SurveyAnswer("Abandoned hospital", 12), SurveyAnswer("Old mansion", 10),
        SurveyAnswer("Dark forest", 8), SurveyAnswer("Creepy hotel", 5),
        SurveyAnswer("Basement", 3), SurveyAnswer("Swamp", 2)))
)

val surveyRounds: List<SurveyRound> = surveyBank

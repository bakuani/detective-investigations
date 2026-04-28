// Mock data for the detective platform

export type Role = 'guest' | 'user' | 'moderator' | 'admin';
export type CaseMode = 'solo' | 'public';
export type CaseStatus = 'active' | 'closed' | 'scoring' | 'scored';
export type GenerationStatus = 'queued' | 'generating' | 'ready' | 'failed';
export type ClueType = 'physical' | 'testimonial' | 'documentary' | 'digital' | 'behavioral';
export type ClueStatus = 'hidden' | 'published';

export interface User {
  id: string;
  nick: string;
  avatar?: string;
  role: Role;
  xp: number;
  rating: number;
  casesPlayed: number;
  casesSolved: number;
  joinedAt: string;
}

export interface Clue {
  id: string;
  caseId: string;
  content: string;
  type: ClueType;
  importance: number; // 1-10, 1=least important
  status: ClueStatus;
  revealAt: string;
  isRedHerring: boolean;
  entityRefs: string[];
}

export interface Character {
  id: string;
  name: string;
  role: string;
  description: string;
}

export interface Case {
  id: string;
  title: string;
  intro: string;
  genre: string;
  theme?: string;
  difficulty: 1 | 2 | 3 | 4 | 5;
  mode: CaseMode;
  status: CaseStatus;
  createdBy: string;
  deadline: string;
  createdAt: string;
  participants: string[];
  characters: Character[];
  clues: Clue[];
  groundTruth?: GroundTruth;
  generationId: string;
  tags: string[];
}

export interface GroundTruth {
  culprit: string;
  motive: string;
  method: string;
  explanation: string;
}

export interface Solution {
  id: string;
  caseId: string;
  userId: string;
  culprit: string;
  motive: string;
  method: string;
  reasoning: string;
  submittedAt: string;
  score?: number;
  breakdown?: ScoreBreakdown;
  place?: number;
}

export interface ScoreBreakdown {
  culprit: number;
  motive: number;
  method: number;
  logic: number;
  explanation: string;
}

export interface ChatMessage {
  id: string;
  caseId: string;
  userId: string;
  userNick: string;
  text: string;
  isSystem: boolean;
  createdAt: string;
  reported?: boolean;
}

export type XpEntryType = 'case_solved' | 'first_place' | 'bonus' | 'participation' | 'daily';

export interface XpHistoryEntry {
  id: string;
  userId: string;
  caseId: string;
  caseTitle: string;
  score: number;
  xpGained: number;
  ratingChange: number;
  date: string;
  type: XpEntryType;
}

export interface GenerationRequest {
  id: string;
  userId: string;
  mode: CaseMode;
  genre: string;
  difficulty: 1 | 2 | 3 | 4 | 5;
  status: GenerationStatus;
  createdAt: string;
  errorMessage?: string;
  resultCaseId?: string;
}

export interface ModerationAction {
  id: string;
  moderatorId: string;
  moderatorNick: string;
  targetType: 'message' | 'user';
  targetId: string;
  action: 'delete' | 'hide' | 'mute' | 'ban' | 'warn';
  reason: string;
  timestamp: string;
}

export interface Report {
  id: string;
  reporterId: string;
  reporterNick: string;
  targetType: 'message' | 'user';
  targetId: string;
  caseId: string;
  reason: string;
  status: 'pending' | 'resolved' | 'dismissed';
  createdAt: string;
}

// ─── MOCK DATA ────────────────────────────────────────────────────────────────

export const MOCK_USERS: User[] = [
  { id: 'u1', nick: 'ШерлокБот', role: 'user', xp: 4820, rating: 1340, casesPlayed: 24, casesSolved: 18, joinedAt: '2024-01-10' },
  { id: 'u2', nick: 'Мисс_Марпл', role: 'user', xp: 7210, rating: 1650, casesPlayed: 41, casesSolved: 35, joinedAt: '2023-11-05' },
  { id: 'u3', nick: 'ПуаройФанат', role: 'user', xp: 2100, rating: 980, casesPlayed: 12, casesSolved: 7, joinedAt: '2024-03-20' },
  { id: 'u4', nick: 'NightOwl99', role: 'user', xp: 3400, rating: 1120, casesPlayed: 18, casesSolved: 12, joinedAt: '2024-02-14' },
  { id: 'u5', nick: 'Детектив_К', role: 'moderator', xp: 9800, rating: 2100, casesPlayed: 60, casesSolved: 52, joinedAt: '2023-08-01' },
  { id: 'u6', nick: 'AdminPrime', role: 'admin', xp: 15000, rating: 2800, casesPlayed: 90, casesSolved: 80, joinedAt: '2023-05-01' },
];

export const MOCK_CHARACTERS_1: Character[] = [
  { id: 'c1', name: 'Граф Александров', role: 'Жертва', description: 'Богатый землевладелец, найденный мёртвым в своём кабинете' },
  { id: 'c2', name: 'Елена Мирова', role: 'Племянница', description: 'Единственная наследница, приехавшая за день до смерти дяди' },
  { id: 'c3', name: 'Борис Краснов', role: 'Дворецкий', description: 'Верный слуга на протяжении 20 лет, хранит много секретов' },
  { id: 'c4', name: 'Доктор Фёдоров', role: 'Врач', description: 'Семейный врач, последний кто видел графа живым' },
  { id: 'c5', name: 'Анна Серова', role: 'Горничная', description: 'Новая прислуга, нанятая месяц назад с поддельными рекомендациями' },
];

export const MOCK_CLUES_1: Clue[] = [
  { id: 'cl1', caseId: 'case1', content: 'В кабинете обнаружен перевёрнутый стакан с остатками бренди. По краям — едва заметный белый осадок.', type: 'physical', importance: 2, status: 'published', revealAt: '2025-01-01T10:00:00Z', isRedHerring: false, entityRefs: ['c1'] },
  { id: 'cl2', caseId: 'case1', content: 'В мусорной корзине найдена скомканная записка: "Я знаю, что ты сделал с завещанием. Встретимся в полночь."', type: 'documentary', importance: 3, status: 'published', revealAt: '2025-01-01T12:00:00Z', isRedHerring: false, entityRefs: ['c1', 'c2'] },
  { id: 'cl3', caseId: 'case1', content: 'Горничная Анна видела доктора Фёдорова выходящим из библиотеки около 23:45, хотя тот утверждал, что ушёл в 22:00.', type: 'testimonial', importance: 4, status: 'published', revealAt: '2025-01-01T14:00:00Z', isRedHerring: false, entityRefs: ['c4', 'c5'] },
  { id: 'cl4', caseId: 'case1', content: 'В аптечке доктора Фёдорова пропала ампула с дигоксином — сердечным препаратом в токсических дозах смертельным.', type: 'physical', importance: 7, status: 'published', revealAt: '2025-01-02T09:00:00Z', isRedHerring: false, entityRefs: ['c4'] },
  { id: 'cl5', caseId: 'case1', content: 'Борис Краснов рассказал, что граф собирался переписать завещание, исключив племянницу. Новый нотариус должен был приехать завтра.', type: 'testimonial', importance: 5, status: 'published', revealAt: '2025-01-02T11:00:00Z', isRedHerring: false, entityRefs: ['c2', 'c3'] },
  { id: 'cl6', caseId: 'case1', content: 'На телефоне Елены Мировой — удалённая переписка с неизвестным номером. Восстановленный фрагмент: "Сделай это сегодня ночью или всё потеряно".', type: 'digital', importance: 8, status: 'hidden', revealAt: '2025-07-15T16:00:00Z', isRedHerring: false, entityRefs: ['c2'] },
  { id: 'cl7', caseId: 'case1', content: 'На подоконнике библиотеки обнаружен женский платок с монограммой "А.С." и запахом дорогих духов.', type: 'physical', importance: 1, status: 'published', revealAt: '2025-01-01T08:00:00Z', isRedHerring: true, entityRefs: ['c5'] },
  { id: 'cl8', caseId: 'case1', content: 'Финансовые документы показывают: доктор Фёдоров взял у графа крупный долг 6 месяцев назад и не вернул ни копейки.', type: 'documentary', importance: 9, status: 'hidden', revealAt: '2025-07-15T20:00:00Z', isRedHerring: false, entityRefs: ['c1', 'c4'] },
];

export const MOCK_CLUES_2: Clue[] = [
  { id: 'cl9', caseId: 'case2', content: 'Камера у входа зафиксировала силуэт в капюшоне, входящий в галерею за 3 минуты до срабатывания сигнализации.', type: 'digital', importance: 2, status: 'published', revealAt: '2025-01-05T10:00:00Z', isRedHerring: false, entityRefs: [] },
  { id: 'cl10', caseId: 'case2', content: 'Похищенная картина застрахована на сумму вдвое выше рыночной. Страховку оформили два месяца назад.', type: 'documentary', importance: 3, status: 'published', revealAt: '2025-01-05T12:00:00Z', isRedHerring: false, entityRefs: [] },
  { id: 'cl11', caseId: 'case2', content: 'Охранник утверждает, что видел директора галереи задержавшимся допоздна. Директор это отрицает.', type: 'testimonial', importance: 6, status: 'published', revealAt: '2025-01-06T09:00:00Z', isRedHerring: false, entityRefs: [] },
];

export const MOCK_CASES: Case[] = [
  {
    id: 'case1',
    title: 'Смерть в особняке Александрова',
    intro: 'Холодным ноябрьским утром граф Виктор Александров найден мёртвым в запертом изнутри кабинете своего загородного особняка. Смерть наступила между 23:00 и 01:00. В доме находились пятеро: племянница, дворецкий, доктор, горничная и садовник. Местная полиция растеряна — помогите распутать это дело.',
    genre: 'Классический',
    difficulty: 3,
    mode: 'public',
    status: 'active',
    createdBy: 'u2',
    deadline: '2025-07-25T23:59:59Z',
    createdAt: '2025-07-10T08:00:00Z',
    participants: ['u1', 'u2', 'u3', 'u4'],
    characters: MOCK_CHARACTERS_1,
    clues: MOCK_CLUES_1,
    groundTruth: {
      culprit: 'Доктор Фёдоров',
      motive: 'Долг и финансовое банкротство',
      method: 'Отравление дигоксином подмешанным в бренди',
      explanation: 'Доктор Фёдоров, осознав, что граф потребует возврата долга после встречи с новым нотариусом, решился на убийство. Воспользовавшись доверием жертвы, он подмешал летальную дозу дигоксина в вечерний бренди во время "дружеского" визита. Платок Анны — случайная улика, не относящаяся к делу.'
    },
    generationId: 'gen_abc123',
    tags: ['яд', 'особняк', 'наследство'],
  },
  {
    id: 'case2',
    title: 'Кража в галерее "Северное сияние"',
    intro: 'Из частной галереи современного искусства исчезло полотно Тихонова "Закат над Балтикой" стоимостью 2,4 млн рублей. Сигнализация сработала в 03:17, однако к приезду полиции вор уже скрылся. Среди подозреваемых — куратор, директор, ночной охранник и реставратор.',
    genre: 'Нуар',
    difficulty: 2,
    mode: 'public',
    status: 'scored',
    createdBy: 'u1',
    deadline: '2025-07-10T23:59:59Z',
    createdAt: '2025-07-01T10:00:00Z',
    participants: ['u1', 'u2', 'u4', 'u5'],
    characters: [
      { id: 'g1', name: 'Марина Власова', role: 'Куратор', description: 'Бывший партнёр художника, расстались не по-хорошему' },
      { id: 'g2', name: 'Игорь Нестеров', role: 'Директор', description: 'Известен финансовыми трудностями и спорными сделками' },
      { id: 'g3', name: 'Пётр Лунин', role: 'Охранник', description: 'Работает в галерее 10 лет, безупречная репутация' },
      { id: 'g4', name: 'Светлана Орлова', role: 'Реставратор', description: 'Специалист по подделкам, знает все тонкости рынка' },
    ],
    clues: MOCK_CLUES_2,
    groundTruth: {
      culprit: 'Игорь Нестеров',
      motive: 'Страховое мошенничество',
      method: 'Инсценировка кражи с сообщником',
      explanation: 'Директор Нестеров, погрязший в долгах, организовал "кражу" собственной картины ради страховой выплаты. Страховку он оформил заблаговременно.'
    },
    generationId: 'gen_def456',
    tags: ['кража', 'искусство', 'мошенничество'],
  },
  {
    id: 'case3',
    title: 'Исчезновение в Чёрном лесу',
    intro: 'Известный биолог Максим Берёзов не вернулся из экспедиции в заповедный лес. Его палатка найдена нетронутой, оборудование в порядке, но сам учёный бесследно исчез. Местные жители говорят о странных огнях по ночам, коллеги — о секретных исследованиях.',
    genre: 'Мистика',
    difficulty: 4,
    mode: 'public',
    status: 'active',
    createdBy: 'u3',
    deadline: '2025-07-30T23:59:59Z',
    createdAt: '2025-07-12T14:00:00Z',
    participants: ['u3', 'u4'],
    characters: [
      { id: 'f1', name: 'Максим Берёзов', role: 'Жертва', description: 'Эколог, изучал аномальную растительность' },
      { id: 'f2', name: 'Профессор Кузьмин', role: 'Научный руководитель', description: 'Конкурирует за грант с Берёзовым' },
      { id: 'f3', name: 'Лесник Дмитрий', role: 'Свидетель', description: 'Видел чужаков в лесу накануне' },
      { id: 'f4', name: 'Нина Берёзова', role: 'Жена', description: 'Нашла в кармане мужа странную записку перед отъездом' },
    ],
    clues: [
      { id: 'fc1', caseId: 'case3', content: 'В дневнике Берёзова последняя запись: "Они знают, что я нашёл. Необходимо успеть передать данные до завтра."', type: 'documentary', importance: 2, status: 'published', revealAt: '2025-07-12T14:00:00Z', isRedHerring: false, entityRefs: ['f1'] },
      { id: 'fc2', caseId: 'case3', content: 'Следы волочения ведут от палатки к старой заброшенной шахте в 800 метрах, но там обрываются.', type: 'physical', importance: 3, status: 'published', revealAt: '2025-07-13T09:00:00Z', isRedHerring: false, entityRefs: [] },
      { id: 'fc3', caseId: 'case3', content: 'GPS-трекер экспедиции показывает: за 2 часа до исчезновения учёный удалился от лагеря на 3 км и вернулся.', type: 'digital', importance: 5, status: 'published', revealAt: '2025-07-14T09:00:00Z', isRedHerring: false, entityRefs: ['f1'] },
      { id: 'fc4', caseId: 'case3', content: 'На подошвах сапог найдена специфическая синяя глина, характерная только для карьера промышленной компании "ЛесРесурс".', type: 'physical', importance: 8, status: 'hidden', revealAt: '2025-07-28T09:00:00Z', isRedHerring: false, entityRefs: [] },
    ],
    generationId: 'gen_ghi789',
    tags: ['пропажа', 'лес', 'тайна'],
  },
  {
    id: 'case4',
    title: 'Яд на корпоративной вечеринке',
    intro: 'На ежегодной вечеринке IT-компании "Nexus Corp" финансовый директор Роман Власов внезапно потерял сознание. Медики зафиксировали признаки отравления. Среди 200 гостей — коллеги, партнёры и конкуренты. Камеры зафиксировали несколько подозрительных моментов у барной стойки.',
    genre: 'Исторический',
    difficulty: 5,
    mode: 'public',
    status: 'active',
    createdBy: 'u1',
    deadline: '2025-08-05T23:59:59Z',
    createdAt: '2025-07-13T16:00:00Z',
    participants: ['u1', 'u2', 'u3', 'u4', 'u5'],
    characters: [
      { id: 'n1', name: 'Роман Власов', role: 'Жертва (выжил)', description: 'CFO компании, знает о финансовых махинациях' },
      { id: 'n2', name: 'Алексей Громов', role: 'CEO', description: 'Давний партнёр Власова, возможно хочет скрыть схему' },
      { id: 'n3', name: 'Карина Ли', role: 'PR-директор', description: 'Недавно рассталась с Власовым, очень напряжена' },
      { id: 'n4', name: 'Тимур Рустамов', role: 'Конкурент', description: 'Представитель другой компании, был приглашён как гость' },
      { id: 'n5', name: 'Бартендер Саша', role: 'Свидетель', description: 'Работал у стойки всю ночь, видел всё' },
    ],
    clues: [
      { id: 'nc1', caseId: 'case4', content: 'Бокал Власова отправлен на экспертизу: обнаружен следовой алкалоид, требующий идентификации.', type: 'physical', importance: 2, status: 'published', revealAt: '2025-07-13T16:00:00Z', isRedHerring: false, entityRefs: ['n1'] },
      { id: 'nc2', caseId: 'case4', content: 'Бартендер Саша вспомнил: один из гостей просил "добавить лёд" в бокал Власова лично, когда стойка была отвлечена.', type: 'testimonial', importance: 3, status: 'published', revealAt: '2025-07-14T10:00:00Z', isRedHerring: false, entityRefs: ['n5'] },
    ],
    generationId: 'gen_jkl012',
    tags: ['яд', 'корпоратив', 'IT'],
  },
];

export const MOCK_CHAT_MESSAGES: ChatMessage[] = [
  { id: 'm1', caseId: 'case1', userId: 'system', userNick: 'Система', text: 'Дело "Смерть в особняке Александрова" открыто. Удачи, детективы!', isSystem: true, createdAt: '2025-07-10T08:00:00Z' },
  { id: 'm2', caseId: 'case1', userId: 'system', userNick: 'Система', text: 'Новая улика раскрыта: "Показания горничной"', isSystem: true, createdAt: '2025-07-10T14:00:00Z' },
  { id: 'm3', caseId: 'case1', userId: 'u1', userNick: 'ШерлокБот', text: 'Обратите внимание на осадок в стакане. Это явно не случайность.', isSystem: false, createdAt: '2025-07-10T15:30:00Z' },
  { id: 'm4', caseId: 'case1', userId: 'u2', userNick: 'Мисс_Марпл', text: 'Интересно, а дворецкий не мог знать о долге? Он же всё слышит.', isSystem: false, createdAt: '2025-07-10T15:45:00Z' },
  { id: 'm5', caseId: 'case1', userId: 'u3', userNick: 'ПуаройФанат', text: 'Версия с племянницей тоже очень убедительна. У неё мотив очевиден.', isSystem: false, createdAt: '2025-07-10T16:00:00Z' },
  { id: 'm6', caseId: 'case1', userId: 'system', userNick: 'Система', text: 'Новая улика раскрыта: "Пропавшая ампула"', isSystem: true, createdAt: '2025-07-11T09:00:00Z' },
  { id: 'm7', caseId: 'case1', userId: 'u4', userNick: 'NightOwl99', text: 'Вот это поворот с ампулой! Теперь медицинская версия выглядит иначе...', isSystem: false, createdAt: '2025-07-11T09:15:00Z' },
  { id: 'm8', caseId: 'case1', userId: 'u1', userNick: 'ШерлокБот', text: 'Пользователь ШерлокБот сдал решение', isSystem: true, createdAt: '2025-07-12T18:00:00Z' },
];

export const MOCK_SOLUTIONS: Solution[] = [
  { id: 's1', caseId: 'case2', userId: 'u1', culprit: 'Игорь Нестеров', motive: 'Страховое мошенничество', method: 'Инсценировка кражи', reasoning: 'Страховка оформлена слишком своевременно. Директор имел финансовые проблемы.', submittedAt: '2025-07-09T15:30:00Z', score: 94, breakdown: { culprit: 25, motive: 25, method: 22, logic: 22, explanation: 'Отличный анализ, верно определены все ключевые элементы. Логика безупречна.' }, place: 1 },
  { id: 's2', caseId: 'case2', userId: 'u2', culprit: 'Игорь Нестеров', motive: 'Страховое мошенничество', method: 'Инсценировка кражи', reasoning: 'Директор явно скрывает что-то. Страховка — главная улика.', submittedAt: '2025-07-09T16:00:00Z', score: 94, breakdown: { culprit: 25, motive: 25, method: 22, logic: 22, explanation: 'Правильный вывод, оба детектива получают максимум.' }, place: 1 },
  { id: 's3', caseId: 'case2', userId: 'u4', culprit: 'Марина Власова', motive: 'Месть', method: 'Кража с использованием дубликата ключа', reasoning: 'Бывший партнёр художника имеет личный мотив.', submittedAt: '2025-07-08T11:00:00Z', score: 32, breakdown: { culprit: 0, motive: 8, method: 12, logic: 12, explanation: 'Мотив правдоподобен, но личность преступника определена неверно.' }, place: 3 },
];

export const MOCK_XP_HISTORY: XpHistoryEntry[] = [
  { id: 'xp1', userId: 'u1', caseId: 'case2', caseTitle: 'Кража в галерее "Северное сияние"', score: 94, xpGained: 470, ratingChange: 85, date: '2025-07-10T20:00:00Z', type: 'first_place' },
  { id: 'xp2', userId: 'u1', caseId: 'case_old1', caseTitle: 'Убийство в поезде', score: 78, xpGained: 312, ratingChange: 45, date: '2025-06-20T20:00:00Z', type: 'case_solved' },
  { id: 'xp3', userId: 'u1', caseId: 'case_old2', caseTitle: 'Тайна пустого дома', score: 45, xpGained: 90, ratingChange: -10, date: '2025-06-05T20:00:00Z', type: 'participation' },
];

export const MOCK_GENERATION_REQUESTS: GenerationRequest[] = [
  { id: 'gr1', userId: 'u1', mode: 'public', genre: 'Классический', difficulty: 3, status: 'ready', createdAt: '2025-07-10T07:50:00Z', resultCaseId: 'case1' },
  { id: 'gr2', userId: 'u3', mode: 'solo', genre: 'Мистика', difficulty: 4, status: 'generating', createdAt: '2025-07-13T18:00:00Z' },
  { id: 'gr3', userId: 'u4', mode: 'public', genre: 'Классический', difficulty: 2, status: 'failed', createdAt: '2025-07-13T17:00:00Z', errorMessage: 'Не удалось сгенерировать достаточный набор улик. Попробуйте другую тему.' },
  { id: 'gr4', userId: 'u2', mode: 'public', genre: 'Киберпанк', difficulty: 5, status: 'queued', createdAt: '2025-07-13T19:00:00Z' },
];

export const MOCK_MODERATION_ACTIONS: ModerationAction[] = [
  { id: 'ma1', moderatorId: 'u5', moderatorNick: 'Детектив_К', targetType: 'message', targetId: 'm_bad1', action: 'delete', reason: 'Спойлер без предупреждения', timestamp: '2025-07-11T10:00:00Z' },
  { id: 'ma2', moderatorId: 'u5', moderatorNick: 'Детектив_К', targetType: 'user', targetId: 'u_bad1', action: 'mute', reason: 'Систематический флуд', timestamp: '2025-07-12T14:00:00Z' },
];

export const MOCK_REPORTS: Report[] = [
  { id: 'r1', reporterId: 'u1', reporterNick: 'ШерлокБот', targetType: 'message', targetId: 'm_bad2', caseId: 'case1', reason: 'Содержит прямой спойлер', status: 'pending', createdAt: '2025-07-13T09:00:00Z' },
  { id: 'r2', reporterId: 'u2', reporterNick: 'Мисс_Марпл', targetType: 'user', targetId: 'u_bad2', caseId: 'case2', reason: 'Оскорбительные комментарии', status: 'resolved', createdAt: '2025-07-12T11:00:00Z' },
];

export const GENRES = [
  'Классический',
  'Нуар',
  'Мистика',
  'Исторический',
  'Киберпанк'
];

export const MOTIVES = [
  'Финансовая выгода',
  'Месть',
  'Страховое мошенничество',
  'Ревность',
  'Сокрытие преступления',
  'Долг и банкротство',
  'Политические амбиции',
  'Личная вражда',
];

export const METHODS = [
  'Отравление',
  'Инсценировка кражи',
  'Физическое насилие',
  'Удар тупым предметом',
  'Взлом и кража',
  'Удушение',
  'Поджог',
  'Технический саботаж',
];
